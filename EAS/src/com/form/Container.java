package com.form;

import com.panel.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class Container extends JFrame {
    private int userId;
    private String userName;
    private String userRole;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private Map<String, Boolean> userPermissions;

    // Panel constants
    private static final String DASHBOARD = "Dashboard";
    private static final String PRODUCTS = "Products";
    private static final String ORDERS = "Orders";
    private static final String USERS = "Users";
    private static final String CATEGORIES = "Categories";
    private static final String REPORTS = "Reports";
    private static final String PAYMENTS = "Payments";
    private static final String SHIPMENTS = "Shipments";
    private static final String REVIEWS = "Reviews";

    public Container(int userId, String userName, String userRole) throws Exception {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.userPermissions = initializePermissions();
        
        initializeUI();
        setupPanels();
    }

    private Map<String, Boolean> initializePermissions() throws Exception {
        Map<String, Boolean> permissions = new HashMap<>();
        
        switch (userRole.toLowerCase()) {
            case "admin":
                permissions.put(DASHBOARD, true);
                permissions.put(PRODUCTS, true);
                permissions.put(ORDERS, true);
                permissions.put(USERS, true);
                permissions.put(CATEGORIES, true);
                permissions.put(REPORTS, true);
                permissions.put(PAYMENTS, true);
                permissions.put(SHIPMENTS, true);
                permissions.put(REVIEWS, true);
                break;
                
            case "seller":
                permissions.put(DASHBOARD, true);
                permissions.put(PRODUCTS, true);
                permissions.put(ORDERS, true);
                permissions.put(USERS, false);
                permissions.put(CATEGORIES, false);
                permissions.put(REPORTS, true);
                permissions.put(PAYMENTS, false);
                permissions.put(SHIPMENTS, true);
                permissions.put(REVIEWS, true);
                break;
                
            case "buyer":
                permissions.put(DASHBOARD, true);
                permissions.put(PRODUCTS, true);
                permissions.put(ORDERS, true);
                permissions.put(USERS, false);
                permissions.put(CATEGORIES, false);
                permissions.put(REPORTS, false);
                permissions.put(PAYMENTS, true);
                permissions.put(SHIPMENTS, false);
                permissions.put(REVIEWS, true);
                break;
                
            default:
                throw new Exception("Invalid user role: " + userRole);
        }
        
        return permissions;
    }

    private void initializeUI() throws Exception {
        setTitle("E-Commerce System - " + userRole);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Create sidebar
        JPanel sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // Create main content area with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createSidebar() throws Exception {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setBackground(new Color(240, 240, 240));

        // User info panel
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(220, 220, 220));
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userInfoPanel.setMaximumSize(new Dimension(180, 80));

        JLabel userLabel = new JLabel(userName);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("(" + userRole + ")");
        roleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(userLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        userInfoPanel.add(roleLabel);

        sidebar.add(userInfoPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // Navigation buttons - only show accessible panels
        if (userPermissions.get(DASHBOARD)) {
            addNavButton(sidebar, "📊 Dashboard", DASHBOARD);
        }
        if (userPermissions.get(PRODUCTS)) {
            addNavButton(sidebar, "📦 Products", PRODUCTS);
        }
        if (userPermissions.get(ORDERS)) {
            addNavButton(sidebar, "📋 Orders", ORDERS);
        }
        if (userPermissions.get(USERS)) {
            addNavButton(sidebar, "👥 User Management", USERS);
        }
        if (userPermissions.get(CATEGORIES)) {
            addNavButton(sidebar, "📑 Categories", CATEGORIES);
        }
        if (userPermissions.get(REPORTS)) {
            addNavButton(sidebar, "📈 Reports", REPORTS);
        }
        if (userPermissions.get(PAYMENTS)) {
            addNavButton(sidebar, "💳 Payments", PAYMENTS);
        }
        if (userPermissions.get(SHIPMENTS)) {
            addNavButton(sidebar, "🚚 Shipments", SHIPMENTS);
        }
        if (userPermissions.get(REVIEWS)) {
            addNavButton(sidebar, "⭐ Reviews", REVIEWS);
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(180, 35));
        logoutBtn.setBackground(new Color(220, 100, 100));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(this::logout);
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String text, String panelName) throws Exception {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 35));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(e -> {
            try {
                showPanel(panelName);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error showing panel: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        sidebar.add(button);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void setupPanels() throws Exception {
        // Initialize all panels based on permissions and user role
        
        // Dashboard Panel - Accessible to all roles
        mainContentPanel.add(new Dashboardpanel(userRole, userName, userId), DASHBOARD);
        
        // Products Panel - Different access levels
        if (userPermissions.get(PRODUCTS)) {
            mainContentPanel.add(new ProductsPanel(userName, userId, userRole), PRODUCTS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Products Management"), PRODUCTS);
        }
        
        // Orders Panel - Different access levels
        if (userPermissions.get(ORDERS)) {
            mainContentPanel.add(new OrderPanel(userId, userRole), ORDERS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Orders Management"), ORDERS);
        }
        
        // Users Panel - Admin only
        if (userPermissions.get(USERS)) {
            mainContentPanel.add(new UserPanel(), USERS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("User Management"), USERS);
        }
        
        // Categories Panel - Admin only
        if (userPermissions.get(CATEGORIES)) {
            mainContentPanel.add(new CategoriesPanel(), CATEGORIES);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Category Management"), CATEGORIES);
        }
        
        // Reports Panel - Admin and Seller only
        if (userPermissions.get(REPORTS)) {
            mainContentPanel.add(new ReportsPanel(userRole), REPORTS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Reports"), REPORTS);
        }
        
        // Payments Panel - Admin and Buyer only
        if (userPermissions.get(PAYMENTS)) {
            mainContentPanel.add(new PaymentPanel(userRole, userId, userName), PAYMENTS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Payment Management"), PAYMENTS);
        }
        
        // Shipments Panel - Admin and Seller only
        if (userPermissions.get(SHIPMENTS)) {
            mainContentPanel.add(new ShipmentPanel(), SHIPMENTS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Shipment Management"), SHIPMENTS);
        }
        
        // Reviews Panel - Accessible to all roles
        if (userPermissions.get(REVIEWS)) {
            mainContentPanel.add(new Reviewpanel(), REVIEWS);
        } else {
            mainContentPanel.add(createAccessDeniedPanel("Reviews"), REVIEWS);
        }
        
        
  
        // Show dashboard by default
        showPanel(DASHBOARD);
    }

    private JPanel createAccessDeniedPanel(String panelName) throws Exception {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 240));
        
        JLabel message = new JLabel(
            "<html><center><h2>🚫 Access Denied</h2>" +
            "<p>You don't have permission to access " + panelName + "</p>" +
            "<p>Your role: <strong>" + userRole + "</strong></p>" +
            "<p>Please contact administrator for access.</p></center></html>", 
            JLabel.CENTER
        );
        message.setFont(new Font("Arial", Font.PLAIN, 14));
        message.setForeground(Color.RED);
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    private void showPanel(String panelName) throws Exception {
        if (userPermissions.getOrDefault(panelName, false)) {
            cardLayout.show(mainContentPanel, panelName);
            setTitle("E-Commerce System - " + userRole + " - " + getPanelDisplayName(panelName));
            
            // Refresh specific panels when shown
            refreshPanelIfNeeded(panelName);
        } else {
            throw new Exception("Access denied to " + getPanelDisplayName(panelName) + 
                              ". Your role (" + userRole + ") does not have permission to access this panel.");
        }
    }

    private void refreshPanelIfNeeded(String panelName) throws Exception {
        // Refresh data for specific panels when they are shown
        switch (panelName) {
            case PRODUCTS:
                // Refresh products panel data
                Component[] productsComponents = mainContentPanel.getComponents();
                for (Component comp : productsComponents) {
                    if (comp instanceof ProductsPanel && comp.isVisible()) {
                        ((ProductsPanel) comp).loadProducts();
                        break;
                    }
                }
                break;
            case ORDERS:
                // Refresh orders panel data
                Component[] ordersComponents = mainContentPanel.getComponents();
                for (Component comp : ordersComponents) {
                    if (comp instanceof OrderPanel && comp.isVisible()) {
                        // If OrderPanel has a refresh method, call it here
                        // ((OrderPanel) comp).refreshData();
                        break;
                    }
                }
                break;
            case DASHBOARD:
                // Refresh dashboard data
                Component[] dashboardComponents = mainContentPanel.getComponents();
                for (Component comp : dashboardComponents) {
                    if (comp instanceof Dashboardpanel && comp.isVisible()) {
                        // If DashboardPanel has a refresh method, call it here
                        // ((Dashboardpanel) comp).refreshData();
                        break;
                    }
                }
                break;
        }
    }

    private String getPanelDisplayName(String panelName) throws Exception {
        switch (panelName) {
            case DASHBOARD: return "Dashboard";
            case PRODUCTS: return "Products Management";
            case ORDERS: return "Orders Management";
            case USERS: return "User Management";
            case CATEGORIES: return "Category Management";
            case REPORTS: return "Reports & Analytics";
            case PAYMENTS: return "Payment Management";
            case SHIPMENTS: return "Shipment Management";
            case REVIEWS: return "Reviews";
            default: 
                throw new Exception("Unknown panel name: " + panelName);
        }
    }

    private void logout(ActionEvent e) {
        try {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                // Assuming you have a LoginForm class
                new Loginform().setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error during logout: " + ex.getMessage(),
                "Logout Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters for user information
    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRole() {
        return userRole;
    }

    // Method to check if user has permission for a specific panel
    public boolean hasPermission(String panelName) throws Exception {
        Boolean permission = userPermissions.get(panelName);
        if (permission == null) {
            throw new Exception("Unknown panel: " + panelName);
        }
        return permission;
    }

    // Method to update user information (if needed)
    public void updateUserInfo(String newUserName) throws Exception {
        if (newUserName == null || newUserName.trim().isEmpty()) {
            throw new Exception("Invalid username provided");
        }
        this.userName = newUserName;
        refreshUserInfoDisplay();
    }

    private void refreshUserInfoDisplay() throws Exception {
        setTitle("E-Commerce System - " + userRole + " - " + userName);
    }

    // Method to reload all panels (useful after major data changes)
    public void reloadAllPanels() throws Exception {
        mainContentPanel.removeAll();
        setupPanels();
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // Method to show a specific panel with error handling
    public void showPanelSafely(String panelName) {
        try {
            showPanel(panelName);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error showing panel: " + ex.getMessage(),
                "Navigation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}