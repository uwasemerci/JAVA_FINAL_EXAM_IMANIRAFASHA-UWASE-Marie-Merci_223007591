package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class UserPanel extends JPanel implements ActionListener {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, addBtn, editBtn, deleteBtn;

    public UserPanel() {
        initializePanel();
        loadUserData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"UserID", "Username", "Email", "FullName", "Role", "LastLogin"};
        tableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(tableModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        refreshBtn = new JButton("Refresh");
        addBtn = new JButton("Add User");
        editBtn = new JButton("Edit User");
        deleteBtn = new JButton("Delete User");

        refreshBtn.addActionListener(this);
        addBtn.addActionListener(this);
        editBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT UserID, Username, Email, FullName, Role, LastLogin FROM User";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Email"),
                    rs.getString("FullName"),
                    rs.getString("Role"),
                    rs.getTimestamp("LastLogin")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadUserData();
        } else if (e.getSource() == addBtn) {
            // Add user functionality
            showAddUserDialog();
        } else if (e.getSource() == editBtn) {
            // Edit user functionality
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                showEditUserDialog(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            }
        } else if (e.getSource() == deleteBtn) {
            // Delete user functionality
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                deleteUser(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            }
        }
    }

    private void showAddUserDialog() {
        // Implementation for adding user
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField fullNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Admin", "Seller", "Buyer"});

        Object[] message = {
            "Username:", usernameField,
            "Email:", emailField,
            "Full Name:", fullNameField,
            "Password:", passwordField,
            "Role:", roleComboBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            addUserToDatabase(usernameField.getText(), emailField.getText(), 
                            fullNameField.getText(), new String(passwordField.getPassword()), 
                            (String) roleComboBox.getSelectedItem());
        }
    }

    private void addUserToDatabase(String username, String email, String fullName, String password, String role) {
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO User (Username, PasswordHash, Email, FullName, Role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, hash this password
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            stmt.setString(5, role);
            stmt.executeUpdate();
            loadUserData();
            JOptionPane.showMessageDialog(this, "User added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage());
        }
    }

    private void showEditUserDialog(int row) {
        // Implementation for editing user
        int userId = (int) tableModel.getValueAt(row, 0);
        JOptionPane.showMessageDialog(this, "Edit user ID: " + userId);
    }

    private void deleteUser(int row) {
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete user: " + username + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DB.getConnection()) {
                String sql = "DELETE FROM User WHERE UserID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                loadUserData();
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }
}