package com.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class Settings extends JPanel implements ActionListener {
    private JTextField systemNameField;
    private JCheckBox emailNotifications;
    private JCheckBox lowStockAlerts;
    private JButton saveBtn;

    public Settings() {
        initializePanel();
        loadCurrentSettings();
    }

    private void initializePanel() {
        setLayout(new GridLayout(5, 2, 10, 10));

        systemNameField = new JTextField();
        emailNotifications = new JCheckBox("Enable Email Notifications");
        lowStockAlerts = new JCheckBox("Enable Low Stock Alerts");
        saveBtn = new JButton("Save Settings");
        saveBtn.addActionListener(this);

        add(new JLabel("System Name:"));
        add(systemNameField);
        add(new JLabel(""));
        add(emailNotifications);
        add(new JLabel(""));
        add(lowStockAlerts);
        add(new JLabel(""));
        add(saveBtn);
    }

    private void loadCurrentSettings() {
        // Load current settings from database or configuration
        systemNameField.setText("E-Commerce System");
        emailNotifications.setSelected(true);
        lowStockAlerts.setSelected(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            saveSettings();
        }
    }

    private void saveSettings() {
        // Save settings to database or configuration file
        String systemName = systemNameField.getText();
        boolean emailEnabled = emailNotifications.isSelected();
        boolean stockAlertsEnabled = lowStockAlerts.isSelected();

        // Here you would typically save to a settings table in database
        JOptionPane.showMessageDialog(this, 
            "Settings saved successfully!\n\n" +
            "System Name: " + systemName + "\n" +
            "Email Notifications: " + (emailEnabled ? "Enabled" : "Disabled") + "\n" +
            "Low Stock Alerts: " + (stockAlertsEnabled ? "Enabled" : "Disabled"));
    }
}