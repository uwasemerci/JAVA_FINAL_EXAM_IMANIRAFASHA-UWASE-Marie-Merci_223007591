package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class Reviewpanel extends JPanel implements ActionListener {
    private JTable reviewTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, addBtn;

    public Reviewpanel() {
        initializePanel();
        loadReviewData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"Product", "Rating", "Comment", "ReviewDate"};
        tableModel = new DefaultTableModel(columns, 0);
        reviewTable = new JTable(tableModel);
        add(new JScrollPane(reviewTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        refreshBtn = new JButton("Refresh");
        addBtn = new JButton("Add Review");

        refreshBtn.addActionListener(this);
        addBtn.addActionListener(this);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReviewData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT p.Name as ProductName, r.Rating, r.Comment, r.CreatedAt " +
                        "FROM Review r INNER JOIN Product p ON r.ProductID = p.ProductID " +
                        "ORDER BY r.CreatedAt DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getString("ProductName"),
                    rs.getInt("Rating"),
                    rs.getString("Comment"),
                    rs.getTimestamp("CreatedAt")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading reviews: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadReviewData();
        } else if (e.getSource() == addBtn) {
            addReview();
        }
    }

    private void addReview() {
        // Implementation for adding review
        JOptionPane.showMessageDialog(this, "Add Review functionality would go here");
    }
}