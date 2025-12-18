package com.form;

import com.util.DB;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Loginform extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private java.awt.Container container;

    public Loginform() {
        setTitle("EAS | Login");
        setSize(340, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        container = getContentPane();
        container.setLayout(new GridBagLayout());
        container.setBackground(new Color(245, 248, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("E_commerce Automation System ", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        container.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        container.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(16);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        container.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        container.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(16);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        container.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 36));
        loginButton.setBackground(new Color(0, 153, 76));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 36));
        cancelButton.setBackground(new Color(204, 0, 0));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        container.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> login());
        cancelButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            String sql = "SELECT UserID, Username, Email, FullName, Role FROM User WHERE Username = ? AND PasswordHash = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("UserID");
                String userRole = rs.getString("Role");
                String fullName = rs.getString("FullName");

                updateLastLogin(conn, username);
                showMessage("Welcome " + fullName + "!\nLogin successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

                openMainContainer(userId, fullName, userRole);
            } else {
                showMessage("Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    private void updateLastLogin(Connection conn, String username) {
        String sql = "UPDATE User SET LastLogin = NOW() WHERE Username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openMainContainer(int userId, String fullName, String userRole) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Avoid conflict with java.awt.Container
                com.form.Container main = new com.form.Container(userId, fullName, userRole);
                main.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showMessage(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }

    // ✔ Added: main method so Eclipse can run this class
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Loginform().setVisible(true);
        });
    }
}
