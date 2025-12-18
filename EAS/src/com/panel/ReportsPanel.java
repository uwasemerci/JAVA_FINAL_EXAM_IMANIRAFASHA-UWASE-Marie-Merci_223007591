package com.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class ReportsPanel extends JPanel implements ActionListener {
    private JComboBox<String> reportTypeCombo;
    private JTextArea reportArea;
    private JButton generateBtn;

    public ReportsPanel(String userRole) {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Report type selection
        JPanel topPanel = new JPanel();
        reportTypeCombo = new JComboBox<>(new String[]{
            "Sales Summary", "Top Products", "Inventory Status", "User Activity"
        });
        generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(this);

        topPanel.add(new JLabel("Report Type:"));
        topPanel.add(reportTypeCombo);
        topPanel.add(generateBtn);

        // Report display area
        reportArea = new JTextArea(20, 50);
        reportArea.setEditable(false);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateBtn) {
            generateReport((String) reportTypeCombo.getSelectedItem());
        }
    }

    private void generateReport(String reportType) {
        try (Connection conn = DB.getConnection()) {
            String sql = "";
            String reportTitle = "";

            switch (reportType) {
                case "Sales Summary":
                    sql = "SELECT DATE(o.Date) as OrderDate, COUNT(*) as OrderCount, SUM(o.TotalAmount) as TotalSales " +
                          "FROM `Order` o WHERE o.Status = 'Delivered' " +
                          "GROUP BY DATE(o.Date) ORDER BY OrderDate DESC LIMIT 30";
                    reportTitle = "Sales Summary (Last 30 Days)";
                    break;
                case "Top Products":
                    sql = "SELECT p.Name, SUM(oi.Quantity) as TotalSold, SUM(oi.TotalPrice) as TotalRevenue " +
                          "FROM OrderItem oi INNER JOIN Product p ON oi.ProductID = p.ProductID " +
                          "INNER JOIN `Order` o ON oi.OrderID = o.OrderID " +
                          "WHERE o.Status = 'Delivered' " +
                          "GROUP BY p.ProductID, p.Name ORDER BY TotalSold DESC LIMIT 10";
                    reportTitle = "Top Selling Products";
                    break;
                case "Inventory Status":
                    sql = "SELECT p.Name, i.Quantity, i.Available, c.Name as Category " +
                          "FROM Inventory i INNER JOIN Product p ON i.ProductID = p.ProductID " +
                          "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
                          "WHERE i.Available < 10 ORDER BY i.Available ASC";
                    reportTitle = "Low Inventory Alert";
                    break;
                case "User Activity":
                    sql = "SELECT u.Username, u.Role, u.LastLogin, COUNT(o.OrderID) as OrderCount " +
                          "FROM User u LEFT JOIN `Order` o ON u.UserID = o.UserID " +
                          "GROUP BY u.UserID, u.Username, u.Role, u.LastLogin " +
                          "ORDER BY u.LastLogin DESC";
                    reportTitle = "User Activity Report";
                    break;
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            StringBuilder report = new StringBuilder();
            report.append(reportTitle).append("\n");
            report.append("=").append("=".repeat(reportTitle.length())).append("\n\n");

            while (rs.next()) {
                switch (reportType) {
                    case "Sales Summary":
                        report.append(rs.getDate("OrderDate")).append(": ")
                              .append(rs.getInt("OrderCount")).append(" orders, $")
                              .append(rs.getDouble("TotalSales")).append("\n");
                        break;
                    case "Top Products":
                        report.append(rs.getString("Name")).append(": ")
                              .append(rs.getInt("TotalSold")).append(" sold, $")
                              .append(rs.getDouble("TotalRevenue")).append("\n");
                        break;
                    case "Inventory Status":
                        report.append(rs.getString("Name")).append(" (")
                              .append(rs.getString("Category")).append("): ")
                              .append(rs.getInt("Available")).append(" available\n");
                        break;
                    case "User Activity":
                        report.append(rs.getString("Username")).append(" (")
                              .append(rs.getString("Role")).append("): ")
                              .append(rs.getTimestamp("LastLogin")).append(", ")
                              .append(rs.getInt("OrderCount")).append(" orders\n");
                        break;
                }
            }

            reportArea.setText(report.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
        }
    }
}