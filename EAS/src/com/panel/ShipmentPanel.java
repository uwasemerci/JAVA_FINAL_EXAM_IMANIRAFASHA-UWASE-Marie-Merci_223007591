package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class ShipmentPanel extends JPanel implements ActionListener {
    private JTable shipmentTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, updateBtn, trackBtn;

    public ShipmentPanel() {
        initializePanel();
        loadShipmentData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"ShipmentID", "OrderNumber", "TrackingNumber", "Carrier", "Status", "ShippedAt"};
        tableModel = new DefaultTableModel(columns, 0);
        shipmentTable = new JTable(tableModel);
        add(new JScrollPane(shipmentTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        refreshBtn = new JButton("Refresh");
        updateBtn = new JButton("Update Status");
        trackBtn = new JButton("Track Shipment");

        refreshBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        trackBtn.addActionListener(this);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(trackBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadShipmentData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT s.ShipmentID, o.OrderNumber, s.TrackingNumber, s.Carrier, s.Status, s.ShippedAt " +
                        "FROM Shipment s INNER JOIN `Order` o ON s.OrderID = o.OrderID";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("ShipmentID"),
                    rs.getString("OrderNumber"),
                    rs.getString("TrackingNumber"),
                    rs.getString("Carrier"),
                    rs.getString("Status"),
                    rs.getTimestamp("ShippedAt")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading shipments: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadShipmentData();
        } else if (e.getSource() == updateBtn) {
            updateShipmentStatus();
        } else if (e.getSource() == trackBtn) {
            trackShipment();
        }
    }

    private void updateShipmentStatus() {
        int selectedRow = shipmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int shipmentId = (int) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);
            
            String[] statusOptions = {"Pending", "Shipped", "Delivered", "Failed"};
            JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
            statusComboBox.setSelectedItem(currentStatus);
            
            JTextField trackingField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            JTextField carrierField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            
            Object[] message = {
                "Tracking Number:", trackingField,
                "Carrier:", carrierField,
                "Status:", statusComboBox
            };
            
            int result = JOptionPane.showConfirmDialog(this, message, 
                    "Update Shipment", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                updateShipmentInDB(shipmentId, trackingField.getText(), carrierField.getText(), 
                                 (String) statusComboBox.getSelectedItem());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a shipment to update.");
        }
    }

    private void updateShipmentInDB(int shipmentId, String trackingNumber, String carrier, String status) {
        try (Connection conn = DB.getConnection()) {
            String sql = "UPDATE Shipment SET TrackingNumber = ?, Carrier = ?, Status = ? WHERE ShipmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, trackingNumber);
            stmt.setString(2, carrier);
            stmt.setString(3, status);
            stmt.setInt(4, shipmentId);
            stmt.executeUpdate();
            loadShipmentData();
            JOptionPane.showMessageDialog(this, "Shipment updated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating shipment: " + e.getMessage());
        }
    }

    private void trackShipment() {
        int selectedRow = shipmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String trackingNumber = (String) tableModel.getValueAt(selectedRow, 2);
            String carrier = (String) tableModel.getValueAt(selectedRow, 3);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            String trackingInfo = "Shipment Tracking Info:\n\n" +
                                "Tracking Number: " + trackingNumber + "\n" +
                                "Carrier: " + carrier + "\n" +
                                "Current Status: " + status + "\n\n" +
                                "Tracking URL: https://tracking.com/" + trackingNumber;
            JOptionPane.showMessageDialog(this, trackingInfo);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a shipment to track.");
        }
    }
}