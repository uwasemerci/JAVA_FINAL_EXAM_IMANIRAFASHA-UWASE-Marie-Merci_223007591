package com.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class Dashboardpanel extends JPanel implements ActionListener {
    private String userRole;
    private String userName;
    private int userId;
    private JPanel statsPanel;
    private JButton refreshBtn;

    public Dashboardpanel(String userRole, String userName, int userId) {
        this.userRole = userRole;
        this.userName = userName;
        this.userId = userId;
        initializePanel();
        loadDashboardData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel with user credentials
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Statistics Panel with grid layout for boxes
        statsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        JScrollPane scrollPane = new JScrollPane(statsPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
            "Dashboard Overview",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 16),
            new Color(70, 130, 180)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);

        // Refresh Button
        refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.addActionListener(this);
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        add(refreshBtn, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // User credentials on the left - loaded from user profile
        JPanel userPanel = createUserProfilePanel();
        
        // Dashboard title on the right
        JLabel dashboardLabel = new JLabel("Dashboard", JLabel.RIGHT);
        dashboardLabel.setFont(new Font("Arial", Font.BOLD, 28));
        dashboardLabel.setForeground(Color.WHITE);
        
        headerPanel.add(userPanel, BorderLayout.WEST);
        headerPanel.add(dashboardLabel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createUserProfilePanel() {
        JPanel userPanel = new JPanel(new GridLayout(2, 1));
        userPanel.setBackground(new Color(70, 130, 180));
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Load actual user profile data from database
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT Username, FullName, Email, Role FROM user WHERE UserID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Use FullName if available, otherwise use Username
                String displayName = rs.getString("FullName");
                if (displayName == null || displayName.trim().isEmpty()) {
                    displayName = rs.getString("Username");
                }
                
                String userRole = rs.getString("Role");
                String userEmail = rs.getString("Email");
                
                JLabel nameLabel = new JLabel(displayName, JLabel.LEFT);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
                nameLabel.setForeground(Color.WHITE);
                
                JLabel roleLabel = new JLabel("(" + userRole + ") - " + userEmail, JLabel.LEFT);
                roleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                roleLabel.setForeground(Color.LIGHT_GRAY);
                
                userPanel.add(nameLabel);
                userPanel.add(roleLabel);
                
                return userPanel;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Fallback if database query fails
        JLabel nameLabel = new JLabel(userName, JLabel.LEFT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        
        JLabel roleLabel = new JLabel("(" + userRole + ")", JLabel.LEFT);
        roleLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        
        userPanel.add(nameLabel);
        userPanel.add(roleLabel);
        
        return userPanel;
    }

    private void createStatBox(String title, String value, String icon, Color color, String exploreAction) {
        JPanel boxPanel = new JPanel(new BorderLayout());
        boxPanel.setBackground(Color.WHITE);
        boxPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        boxPanel.setPreferredSize(new Dimension(200, 120));

        // Title and icon
        JLabel titleLabel = new JLabel(icon + " " + title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        boxPanel.add(titleLabel, BorderLayout.NORTH);

        // Value
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.DARK_GRAY);
        boxPanel.add(valueLabel, BorderLayout.CENTER);

        // Explore button
        JButton exploreBtn = new JButton("Explore");
        exploreBtn.setBackground(color);
        exploreBtn.setForeground(Color.WHITE);
        exploreBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exploreBtn.addActionListener(e -> handleExploreAction(exploreAction));
        
        boxPanel.add(exploreBtn, BorderLayout.SOUTH);

        statsPanel.add(boxPanel);
    }

    private void handleExploreAction(String action) {
        try (Connection conn = DB.getConnection()) {
            switch (action) {
                case "users":
                    showUsersData(conn);
                    break;
                case "products":
                    showProductsData(conn);
                    break;
                case "orders":
                    showOrdersData(conn);
                    break;
                case "revenue":
                    showRevenueData(conn);
                    break;
                case "categories":
                    showCategoriesData(conn);
                    break;
                case "payments":
                    showPaymentsData(conn);
                    break;
                case "shipments":
                    showShipmentsData(conn);
                    break;
                case "reviews":
                    showReviewsData(conn);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Exploring: " + action, "Explore", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void showUsersData(Connection conn) throws SQLException {
        String sql = "SELECT UserID, Username, FullName, Email, Role, CreatedAt FROM user ORDER BY CreatedAt DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Users (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>ID</th><th>Username</th><th>Full Name</th><th>Email</th><th>Role</th><th>Registered</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("UserID")).append("</td>")
                .append("<td>").append(rs.getString("Username")).append("</td>")
                .append("<td>").append(rs.getString("FullName")).append("</td>")
                .append("<td>").append(rs.getString("Email")).append("</td>")
                .append("<td>").append(rs.getString("Role")).append("</td>")
                .append("<td>").append(rs.getTimestamp("CreatedAt")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Users Data", data.toString());
    }

    private void showProductsData(Connection conn) throws SQLException {
        String sql = "SELECT p.ProductID, p.Name, c.Name as Category, p.PriceOrValue, i.Available as Stock, p.Status " +
                     "FROM product p " +
                     "LEFT JOIN category c ON p.CategoryID = c.CategoryID " +
                     "LEFT JOIN inventory i ON p.ProductID = i.ProductID " +
                     "ORDER BY p.ProductID DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Products (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>ID</th><th>Product Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("ProductID")).append("</td>")
                .append("<td>").append(rs.getString("Name")).append("</td>")
                .append("<td>").append(rs.getString("Category")).append("</td>")
                .append("<td>FRW").append(String.format("%.2f", rs.getDouble("PriceOrValue"))).append("</td>")
                .append("<td>").append(rs.getInt("Stock")).append("</td>")
                .append("<td>").append(rs.getString("Status")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Products Data", data.toString());
    }

    private void showOrdersData(Connection conn) throws SQLException {
        String sql = "SELECT o.OrderID, o.OrderNumber, u.FullName as CustomerName, o.TotalAmount, o.Status, o.CreatedAt " +
                     "FROM `order` o " +
                     "LEFT JOIN user u ON o.UserID = u.UserID " +
                     "ORDER BY o.CreatedAt DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Orders (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>Order ID</th><th>Order Number</th><th>Customer</th><th>Amount</th><th>Status</th><th>Order Date</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("OrderID")).append("</td>")
                .append("<td>").append(rs.getString("OrderNumber")).append("</td>")
                .append("<td>").append(rs.getString("CustomerName")).append("</td>")
                .append("<td>FRW").append(String.format("%.2f", rs.getDouble("TotalAmount"))).append("</td>")
                .append("<td>").append(rs.getString("Status")).append("</td>")
                .append("<td>").append(rs.getTimestamp("CreatedAt")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Orders Data", data.toString());
    }

    private void showRevenueData(Connection conn) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) as total_orders, " +
                     "SUM(TotalAmount) as total_revenue, " +
                     "AVG(TotalAmount) as avg_order_value, " +
                     "MAX(TotalAmount) as max_order " +
                     "FROM `order` WHERE Status = 'Delivered'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            StringBuilder data = new StringBuilder();
            data.append("<html><b>Revenue Summary</b><br><br>");
            data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            data.append("<tr style='background-color: #f2f2f2;'><th>Metric</th><th>Value</th></tr>")
                .append("<tr><td>Total Orders Delivered</td><td>").append(rs.getInt("total_orders")).append("</td></tr>")
                .append("<tr><td>Total Revenue</td><td>RFW").append(String.format("%.2f", rs.getDouble("total_revenue"))).append("</td></tr>")
                .append("<tr><td>Average Order Value</td><td>RFW").append(String.format("%.2f", rs.getDouble("avg_order_value"))).append("</td></tr>")
                .append("<tr><td>Highest Order Value</td><td>RFW").append(String.format("%.2f", rs.getDouble("max_order"))).append("</td></tr>")
                .append("</table></html>");
            
            showDataDialog("Revenue Data", data.toString());
        }
    }

    private void showCategoriesData(Connection conn) throws SQLException {
        String sql = "SELECT c.CategoryID, c.Name as CategoryName, COUNT(p.ProductID) as ProductCount " +
                     "FROM category c " +
                     "LEFT JOIN product p ON c.CategoryID = p.CategoryID " +
                     "GROUP BY c.CategoryID, c.Name " +
                     "ORDER BY c.Name";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Product Categories</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>ID</th><th>Category Name</th><th>Product Count</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("CategoryID")).append("</td>")
                .append("<td>").append(rs.getString("CategoryName")).append("</td>")
                .append("<td>").append(rs.getInt("ProductCount")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Categories Data", data.toString());
    }

    private void showPaymentsData(Connection conn) throws SQLException {
        String sql = "SELECT p.PaymentID, op.OrderID, p.Amount, p.Type as PaymentMethod, p.Status, p.Date as PaymentDate " +
                     "FROM payment p " +
                     "LEFT JOIN orderpayment op ON p.PaymentID = op.PaymentID " +
                     "ORDER BY p.Date DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Payments (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>Payment ID</th><th>Order ID</th><th>Amount</th><th>Method</th><th>Status</th><th>Date</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("PaymentID")).append("</td>")
                .append("<td>").append(rs.getInt("OrderID")).append("</td>")
                .append("<td>RFW").append(String.format("%.2f", rs.getDouble("Amount"))).append("</td>")
                .append("<td>").append(rs.getString("PaymentMethod")).append("</td>")
                .append("<td>").append(rs.getString("Status")).append("</td>")
                .append("<td>").append(rs.getTimestamp("PaymentDate")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Payments Data", data.toString());
    }

    private void showShipmentsData(Connection conn) throws SQLException {
        String sql = "SELECT s.ShipmentID, s.OrderID, s.TrackingNumber, s.Carrier, s.Status, s.ShippedAt, s.DeliveredAt " +
                     "FROM shipment s " +
                     "ORDER BY s.CreatedAt DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Shipments (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>Shipment ID</th><th>Order ID</th><th>Tracking Number</th><th>Carrier</th><th>Status</th><th>Shipped At</th><th>Delivered At</th></tr>");
        
        while (rs.next()) {
            data.append("<tr>")
                .append("<td>").append(rs.getInt("ShipmentID")).append("</td>")
                .append("<td>").append(rs.getInt("OrderID")).append("</td>")
                .append("<td>").append(rs.getString("TrackingNumber")).append("</td>")
                .append("<td>").append(rs.getString("Carrier")).append("</td>")
                .append("<td>").append(rs.getString("Status")).append("</td>")
                .append("<td>").append(rs.getTimestamp("ShippedAt")).append("</td>")
                .append("<td>").append(rs.getTimestamp("DeliveredAt")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Shipments Data", data.toString());
    }

    private void showReviewsData(Connection conn) throws SQLException {
        String sql = "SELECT r.reviewid, r.productid, p.Name as ProductName, r.userid, u.Username, r.rating, r.comment, r.createdat " +
                     "FROM review r " +
                     "LEFT JOIN product p ON r.productid = p.ProductID " +
                     "LEFT JOIN user u ON r.userid = u.UserID " +
                     "ORDER BY r.createdat DESC LIMIT 20";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder data = new StringBuilder();
        data.append("<html><b>Recent Reviews (Last 20)</b><br><br>");
        data.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        data.append("<tr style='background-color: #f2f2f2;'><th>Review ID</th><th>Product</th><th>User</th><th>Rating</th><th>Comment</th><th>Date</th></tr>");
        
        while (rs.next()) {
            String comment = rs.getString("comment");
            if (comment != null && comment.length() > 50) {
                comment = comment.substring(0, 50) + "...";
            }
            
            // Create star rating display
            int rating = rs.getInt("rating");
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < rating; i++) {
                stars.append("★");
            }
            for (int i = rating; i < 5; i++) {
                stars.append("☆");
            }
            
            data.append("<tr>")
                .append("<td>").append(rs.getInt("reviewid")).append("</td>")
                .append("<td>").append(rs.getString("ProductName")).append("</td>")
                .append("<td>").append(rs.getString("Username")).append("</td>")
                .append("<td>").append(stars.toString()).append(" (").append(rating).append(")</td>")
                .append("<td>").append(comment != null ? comment : "").append("</td>")
                .append("<td>").append(rs.getTimestamp("createdat")).append("</td>")
                .append("</tr>");
        }
        data.append("</table></html>");
        
        showDataDialog("Reviews Data", data.toString());
    }

    private void showDataDialog(String title, String content) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(content);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void loadDashboardData() {
        statsPanel.removeAll(); // Clear existing boxes
        
        try (Connection conn = DB.getConnection()) {
            String sql = "";
            switch (userRole) {
                case "Admin":
                    sql = buildAdminQuery();
                    break;
                case "Seller":
                    sql = buildSellerQuery();
                    break;
                case "Buyer":
                    sql = buildBuyerQuery();
                    break;
                default:
                    sql = buildAdminQuery(); // Default to admin query for other roles
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            if (!userRole.equals("Admin")) {
                stmt.setInt(1, userId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                displayStatistics(rs);
            } else {
                createStatBox("No Data", "0", "📊", Color.GRAY, "none");
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        } catch (Exception e) {
            showGenericError(e);
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private String buildAdminQuery() {
        return "SELECT " +
               "(SELECT COUNT(*) FROM user) as total_users, " +
               "(SELECT COUNT(*) FROM product) as total_products, " +
               "(SELECT COUNT(*) FROM `order`) as total_orders, " +
               "(SELECT COALESCE(SUM(TotalAmount), 0) FROM `order` WHERE Status = 'Delivered') as total_revenue, " +
               "(SELECT COUNT(*) FROM category) as total_categories, " +
               "(SELECT COUNT(*) FROM payment) as total_payments, " +
               "(SELECT COUNT(*) FROM shipment) as total_shipments, " +
               "(SELECT COUNT(*) FROM review) as total_reviews";
    }

    private String buildSellerQuery() {
        return "SELECT " +
               "(SELECT COUNT(*) FROM product p " +
               " INNER JOIN userproductaccess upa ON p.ProductID = upa.ProductID " +
               " WHERE upa.UserID = ? AND upa.AccessRole IN ('Owner', 'Editor')) as my_products, " +
               "(SELECT COUNT(*) FROM `order` o " +
               " INNER JOIN orderitem oi ON o.OrderID = oi.OrderID " +
               " INNER JOIN userproductaccess upa ON oi.ProductID = upa.ProductID " +
               " WHERE upa.UserID = ? AND upa.AccessRole IN ('Owner', 'Editor')) as my_orders, " +
               "(SELECT COALESCE(SUM(oi.TotalPrice), 0) FROM `order` o " +
               " INNER JOIN orderitem oi ON o.OrderID = oi.OrderID " +
               " INNER JOIN userproductaccess upa ON oi.ProductID = upa.ProductID " +
               " WHERE upa.UserID = ? AND upa.AccessRole IN ('Owner', 'Editor') AND o.Status = 'Delivered') as my_revenue, " +
               "(SELECT COUNT(*) FROM review r " +
               " INNER JOIN userproductaccess upa ON r.productid = upa.ProductID " +
               " WHERE upa.UserID = ? AND upa.AccessRole IN ('Owner', 'Editor')) as my_reviews";
    }

    private String buildBuyerQuery() {
        return "SELECT " +
               "(SELECT COUNT(*) FROM `order` WHERE UserID = ?) as my_orders, " +
               "(SELECT COUNT(*) FROM `order` WHERE UserID = ? AND Status = 'Pending') as pending_orders, " +
               "(SELECT COALESCE(SUM(TotalAmount), 0) FROM `order` WHERE UserID = ?) as total_spent, " +
               "(SELECT COUNT(*) FROM payment p " +
               " INNER JOIN orderpayment op ON p.PaymentID = op.PaymentID " +
               " INNER JOIN `order` o ON op.OrderID = o.OrderID " +
               " WHERE o.UserID = ?) as my_payments, " +
               "(SELECT COUNT(*) FROM review WHERE userid = ?) as my_reviews";
    }

    private void displayStatistics(ResultSet rs) throws SQLException {
        switch (userRole) {
            case "Admin":
                createStatBox("Total Users", String.valueOf(rs.getInt("total_users")), "👥", new Color(41, 128, 185), "users");
                createStatBox("Total Products", String.valueOf(rs.getInt("total_products")), "📦", new Color(39, 174, 96), "products");
                createStatBox("Total Orders", String.valueOf(rs.getInt("total_orders")), "📋", new Color(230, 126, 34), "orders");
                createStatBox("Total Revenue", "RFW" + String.format("%.2f", rs.getDouble("total_revenue")), "💰", new Color(46, 204, 113), "revenue");
                createStatBox("Categories", String.valueOf(rs.getInt("total_categories")), "📁", new Color(142, 68, 173), "categories");
                createStatBox("Payments", String.valueOf(rs.getInt("total_payments")), "💳", new Color(22, 160, 133), "payments");
                createStatBox("Shipments", String.valueOf(rs.getInt("total_shipments")), "🚚", new Color(211, 84, 0), "shipments");
                createStatBox("Reviews", String.valueOf(rs.getInt("total_reviews")), "⭐", new Color(243, 156, 18), "reviews");
                break;
            case "Seller":
                createStatBox("My Products", String.valueOf(rs.getInt("my_products")), "📦", new Color(39, 174, 96), "products");
                createStatBox("Orders Received", String.valueOf(rs.getInt("my_orders")), "📋", new Color(230, 126, 34), "orders");
                createStatBox("Total Revenue", "RFW" + String.format("%.2f", rs.getDouble("my_revenue")), "💰", new Color(46, 204, 113), "revenue");
                createStatBox("Product Reviews", String.valueOf(rs.getInt("my_reviews")), "⭐", new Color(243, 156, 18), "reviews");
                break;
            case "Buyer":
                createStatBox("Total Orders", String.valueOf(rs.getInt("my_orders")), "📋", new Color(230, 126, 34), "orders");
                createStatBox("Pending Orders", String.valueOf(rs.getInt("pending_orders")), "⏳", new Color(231, 76, 60), "orders");
                createStatBox("Total Spent", "RFW" + String.format("%.2f", rs.getDouble("total_spent")), "💰", new Color(46, 204, 113), "revenue");
                createStatBox("My Payments", String.valueOf(rs.getInt("my_payments")), "💳", new Color(22, 160, 133), "payments");
                createStatBox("My Reviews", String.valueOf(rs.getInt("my_reviews")), "⭐", new Color(243, 156, 18), "reviews");
                break;
        }
    }

    private void showDatabaseError(SQLException e) {
        String errorMessage = "<html><div style='text-align: center; color: red; padding: 20px;'>" +
                            "<b>Database Error</b><br><br>" +
                            "Error: " + e.getMessage() + "<br><br>" +
                            "Please check your database connection and table structure." +
                            "</div></html>";
        
        JLabel errorLabel = new JLabel(errorMessage, JLabel.CENTER);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsPanel.add(errorLabel);
    }

    private void showGenericError(Exception e) {
        JLabel errorLabel = new JLabel("<html><div style='text-align: center; color: red;'>Error: " + e.getMessage() + "</div></html>", JLabel.CENTER);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(errorLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadDashboardData();
            JOptionPane.showMessageDialog(this, "Dashboard refreshed successfully!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}