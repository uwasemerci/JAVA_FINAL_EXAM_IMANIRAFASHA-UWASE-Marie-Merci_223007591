package com.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;
import java.util.regex.Pattern;

public class ProfilePanel extends JPanel implements ActionListener {
    private int userId;
    private String userRole;
    private String userName;
    
    // Form components
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton updateProfileBtn;
    private JButton changePasswordBtn;
    private JButton resetBtn;
    private JLabel messageLabel;
    
    // Profile info labels
    private JLabel registrationDateLabel;
    private JLabel lastLoginLabel;
    private JLabel userRoleLabel;

    public ProfilePanel(int userId, String userRole, String userName) {
        this.userId = userId;
        this.userRole = userRole;
        this.userName = userName;
        initializePanel();
        loadProfileData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        // Profile Information Panel
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel);
        
        // Edit Profile Panel
        JPanel editPanel = createEditPanel();
        contentPanel.add(editPanel);
        
        add(contentPanel, BorderLayout.CENTER);

        // Message label
        messageLabel = new JLabel("", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        add(messageLabel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("My Profile", JLabel.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Manage your profile settings", JLabel.RIGHT);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
            "Profile Information",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            new Color(70, 130, 180)
        ));
        
        JPanel infoContent = new JPanel(new GridLayout(0, 1, 5, 10));
        infoContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // User info labels
        JLabel userTitle = new JLabel("User Details", JLabel.CENTER);
        userTitle.setFont(new Font("Arial", Font.BOLD, 16));
        userTitle.setForeground(new Color(70, 130, 180));
        
        JLabel usernameTitle = new JLabel("Username:");
        usernameTitle.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel usernameValue = new JLabel(userName);
        usernameValue.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel emailTitle = new JLabel("Email:");
        emailTitle.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel emailValue = new JLabel("Loading...");
        emailValue.setFont(new Font("Arial", Font.PLAIN, 12));
        
        userRoleLabel = new JLabel("Role: " + userRole);
        userRoleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        userRoleLabel.setForeground(new Color(39, 174, 96));
        
        registrationDateLabel = new JLabel("Member since: Loading...");
        registrationDateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        lastLoginLabel = new JLabel("Last login: Loading...");
        lastLoginLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Add components to info panel
        infoContent.add(userTitle);
        infoContent.add(createSeparator());
        infoContent.add(usernameTitle);
        infoContent.add(usernameValue);
        infoContent.add(emailTitle);
        infoContent.add(emailValue);
        infoContent.add(userRoleLabel);
        infoContent.add(createSeparator());
        infoContent.add(registrationDateLabel);
        infoContent.add(lastLoginLabel);
        
        infoPanel.add(infoContent, BorderLayout.CENTER);
        
        return infoPanel;
    }

    private JPanel createEditPanel() {
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 120, 60), 2), 
            "Edit Profile",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            new Color(220, 120, 60)
        ));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 12));
        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Password change section
        JLabel passwordTitle = new JLabel("Change Password (optional):");
        passwordTitle.setFont(new Font("Arial", Font.BOLD, 12));
        passwordTitle.setForeground(new Color(220, 120, 60));
        
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        currentPasswordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        currentPasswordField = new JPasswordField();
        
        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        newPasswordField = new JPasswordField();
        
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        confirmPasswordField = new JPasswordField();
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        updateProfileBtn = new JButton("Update Profile");
        updateProfileBtn.setBackground(new Color(70, 130, 180));
        updateProfileBtn.setForeground(Color.WHITE);
        updateProfileBtn.addActionListener(this);
        
        resetBtn = new JButton("Reset");
        resetBtn.setBackground(new Color(120, 120, 120));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.addActionListener(this);
        
        buttonPanel.add(updateProfileBtn);
        buttonPanel.add(resetBtn);
        
        // Add components to form
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(createSeparator());
        formPanel.add(passwordTitle);
        formPanel.add(currentPasswordLabel);
        formPanel.add(currentPasswordField);
        formPanel.add(newPasswordLabel);
        formPanel.add(newPasswordField);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(confirmPasswordField);
        formPanel.add(buttonPanel);
        
        editPanel.add(formPanel, BorderLayout.CENTER);
        
        return editPanel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Color.LIGHT_GRAY);
        return separator;
    }

    private void loadProfileData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT Username, Email, RegistrationDate, LastLogin FROM User WHERE UserID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Update form fields
                usernameField.setText(rs.getString("Username"));
                emailField.setText(rs.getString("Email"));
                
                // Update info panel
                Component[] components = ((JPanel)((JPanel)getComponent(1)).getComponent(0)).getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        Component[] innerComps = ((JPanel)comp).getComponents();
                        for (int i = 0; i < innerComps.length; i++) {
                            if (innerComps[i] instanceof JLabel) {
                                JLabel label = (JLabel) innerComps[i];
                                if (label.getText().equals("Loading...")) {
                                    // Find the previous label to determine what this value represents
                                    if (i > 0 && innerComps[i-1] instanceof JLabel) {
                                        JLabel titleLabel = (JLabel) innerComps[i-1];
                                        if (titleLabel.getText().equals("Email:")) {
                                            label.setText(rs.getString("Email"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Update registration and login dates
                registrationDateLabel.setText("Member since: " + 
                    (rs.getDate("RegistrationDate") != null ? 
                     rs.getDate("RegistrationDate").toString() : "N/A"));
                
                lastLoginLabel.setText("Last login: " + 
                    (rs.getTimestamp("LastLogin") != null ? 
                     rs.getTimestamp("LastLogin").toString() : "Never"));
            }
        } catch (Exception e) {
            showMessage("Error loading profile data: " + e.getMessage(), true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateProfileBtn) {
            updateProfile();
        } else if (e.getSource() == resetBtn) {
            resetForm();
        }
    }

    private void updateProfile() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty() || email.isEmpty()) {
            showMessage("Username and email are required!", true);
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address!", true);
            return;
        }

        // Password change validation
        boolean changingPassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();
        if (changingPassword) {
            if (currentPassword.isEmpty()) {
                showMessage("Current password is required to change password!", true);
                return;
            }
            if (newPassword.isEmpty()) {
                showMessage("New password is required!", true);
                return;
            }
            if (confirmPassword.isEmpty()) {
                showMessage("Please confirm your new password!", true);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showMessage("New passwords do not match!", true);
                return;
            }
            if (newPassword.length() < 6) {
                showMessage("New password must be at least 6 characters long!", true);
                return;
            }
        }

        try (Connection conn = DB.getConnection()) {
            // Verify current password if changing password
            if (changingPassword) {
                String verifySql = "SELECT Password FROM User WHERE UserID = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
                verifyStmt.setInt(1, userId);
                ResultSet rs = verifyStmt.executeQuery();
                
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    // In real application, you should use proper password hashing
                    if (!storedPassword.equals(currentPassword)) {
                        showMessage("Current password is incorrect!", true);
                        return;
                    }
                }
            }

            // Update profile
            String updateSql;
            if (changingPassword) {
                updateSql = "UPDATE User SET Username = ?, Email = ?, Password = ?, LastUpdated = NOW() WHERE UserID = ?";
            } else {
                updateSql = "UPDATE User SET Username = ?, Email = ?, LastUpdated = NOW() WHERE UserID = ?";
            }
            
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, username);
            updateStmt.setString(2, email);
            if (changingPassword) {
                updateStmt.setString(3, newPassword); // In real app, hash this password
                updateStmt.setInt(4, userId);
            } else {
                updateStmt.setInt(3, userId);
            }
            
            int rowsAffected = updateStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showMessage("Profile updated successfully!", false);
                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                // Reload profile data to reflect changes
                loadProfileData();
            } else {
                showMessage("Failed to update profile!", true);
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("Username")) {
                    showMessage("Username already exists! Please choose a different one.", true);
                } else if (e.getMessage().contains("Email")) {
                    showMessage("Email already exists! Please use a different email.", true);
                }
            } else {
                showMessage("Database error: " + e.getMessage(), true);
            }
        } catch (Exception e) {
            showMessage("Error updating profile: " + e.getMessage(), true);
        }
    }

    private void resetForm() {
        loadProfileData();
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        showMessage("Form reset to current values", false);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setForeground(new Color(39, 174, 96));
        }
        
        // Clear message after 5 seconds
        Timer timer = new Timer(5000, e -> messageLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
}