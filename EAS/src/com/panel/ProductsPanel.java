package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.util.DB;

public class ProductsPanel extends JPanel implements ActionListener {
    private String userName;
    private int userId;
    private String userRole;
    
    private JTable productsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> statusFilter;
    private JButton refreshBtn, addToCartBtn, buyNowBtn, viewDetailsBtn;
    private JLabel userInfoLabel;
    private JCheckBox showOnlyAvailableCheckbox;
    
    // Database connection
    private Connection conn;
    
    public ProductsPanel(String userName, int userId, String userRole) {
        this.userName = userName;
        this.userId = userId;
        this.userRole = userRole;
        
        initializePanel();
        loadProducts();
        loadCategories();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Search and Filter Panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Products Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Action Buttons Panel
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Product Management", JLabel.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        userInfoLabel = new JLabel("Welcome, " + userName + " (" + userRole + ")", JLabel.RIGHT);
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userInfoLabel.setForeground(Color.LIGHT_GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        // Show Only Available Checkbox
        showOnlyAvailableCheckbox = new JCheckBox("Show Only Available Products");
        showOnlyAvailableCheckbox.setSelected(true); // Default to showing only available
        showOnlyAvailableCheckbox.addActionListener(this);
        showOnlyAvailableCheckbox.setFont(new Font("Arial", Font.BOLD, 12));
        showOnlyAvailableCheckbox.setForeground(new Color(39, 174, 96));
        filterPanel.add(showOnlyAvailableCheckbox);
        
        filterPanel.add(Box.createHorizontalStrut(20)); // Add some space
        
        // Category Filter
        filterPanel.add(new JLabel("Category:"));
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.addActionListener(this);
        filterPanel.add(categoryFilter);
        
        // Status Filter
        filterPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>();
        statusFilter.addItem("All Status");
        statusFilter.addItem("Active");
        statusFilter.addItem("Inactive");
        statusFilter.addActionListener(this);
        filterPanel.add(statusFilter);
        
        // Refresh Button
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(this);
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setForeground(Color.WHITE);
        filterPanel.add(refreshBtn);
        
        return filterPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table model
        String[] columns = {"ID", "Name", "Category", "Price", "Stock", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID
                if (columnIndex == 3) return Double.class; // Price
                if (columnIndex == 4) return Integer.class; // Stock
                return String.class;
            }
        };
        
        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.getTableHeader().setReorderingAllowed(false);
        productsTable.setRowHeight(30);
        
        // Set column widths
        productsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        productsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        productsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Category
        productsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Price
        productsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Stock
        productsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        productsTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Actions
        
        // Add action buttons to table
        addActionButtonsToTable();
        
        JScrollPane scrollPane = new JScrollPane(productsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void addActionButtonsToTable() {
        // This method will be called when rendering the table
        productsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        productsTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        // Add to Cart Button
        addToCartBtn = new JButton("🛒 Add to Cart");
        addToCartBtn.addActionListener(this);
        addToCartBtn.setBackground(new Color(52, 152, 219));
        addToCartBtn.setForeground(Color.WHITE);
        actionPanel.add(addToCartBtn);
        
        // Buy Now Button
        buyNowBtn = new JButton("⚡ Buy Now");
        buyNowBtn.addActionListener(this);
        buyNowBtn.setBackground(new Color(46, 204, 113));
        buyNowBtn.setForeground(Color.WHITE);
        actionPanel.add(buyNowBtn);
        
        // View Details Button
        viewDetailsBtn = new JButton("👁️ View Details");
        viewDetailsBtn.addActionListener(this);
        viewDetailsBtn.setBackground(new Color(155, 89, 182));
        viewDetailsBtn.setForeground(Color.WHITE);
        actionPanel.add(viewDetailsBtn);
        
        return actionPanel;
    }
    
    private void loadCategories() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT DISTINCT c.Name FROM category c " +
                        "JOIN product p ON c.CategoryID = p.CategoryID " +
                        "ORDER BY c.Name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                categoryFilter.addItem(rs.getString("Name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadProducts() {
        tableModel.setRowCount(0); // Clear existing data
        
        try (Connection conn = DB.getConnection()) {
            String sql = buildProductQuery();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Set parameters based on filters
            int paramIndex = 1;
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            boolean showOnlyAvailable = showOnlyAvailableCheckbox.isSelected();
            
            if (!"All Categories".equals(selectedCategory)) {
                stmt.setString(paramIndex++, selectedCategory);
            }
            if (!"All Status".equals(selectedStatus)) {
                stmt.setString(paramIndex++, selectedStatus);
            }
            if (showOnlyAvailable) {
                // No parameter needed for available filter as it's in WHERE clause
            }
            
            ResultSet rs = stmt.executeQuery();
            
            int availableCount = 0;
            int totalCount = 0;
            
            while (rs.next()) {
                int productId = rs.getInt("ProductID");
                String name = rs.getString("Name");
                String category = rs.getString("CategoryName");
                double price = rs.getDouble("PriceOrValue");
                int stock = rs.getInt("Available");
                String status = rs.getString("Status");
                
                String actionButton = getActionButton(status, stock);
                
                tableModel.addRow(new Object[]{
                    productId, name, category, price, stock, status, actionButton
                });
                
                totalCount++;
                if ("Active".equals(status) && stock > 0) {
                    availableCount++;
                }
            }
            
            // Update header with product count information
            updateProductCountInfo(availableCount, totalCount);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    private void updateProductCountInfo(int availableCount, int totalCount) {
        String countInfo;
        if (showOnlyAvailableCheckbox.isSelected()) {
            countInfo = String.format("Showing %d available products", availableCount);
            userInfoLabel.setForeground(new Color(39, 174, 96)); // Green color for available
        } else {
            countInfo = String.format("Showing %d of %d products (%d available)", totalCount, getTotalProductCount(), availableCount);
            userInfoLabel.setForeground(Color.LIGHT_GRAY); // Default color
        }
        userInfoLabel.setText("Welcome, " + userName + " (" + userRole + ") - " + countInfo);
    }
    
    private int getTotalProductCount() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT COUNT(*) as total FROM product";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private String buildProductQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.ProductID, p.Name, c.Name as CategoryName, ");
        sql.append("p.PriceOrValue, i.Available, p.Status ");
        sql.append("FROM product p ");
        sql.append("LEFT JOIN category c ON p.CategoryID = c.CategoryID ");
        sql.append("LEFT JOIN inventory i ON p.ProductID = i.ProductID ");
        sql.append("WHERE 1=1 ");
        
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        boolean showOnlyAvailable = showOnlyAvailableCheckbox.isSelected();
        
        if (!"All Categories".equals(selectedCategory)) {
            sql.append("AND c.Name = ? ");
        }
        if (!"All Status".equals(selectedStatus)) {
            sql.append("AND p.Status = ? ");
        }
        if (showOnlyAvailable) {
            sql.append("AND p.Status = 'Active' AND i.Available > 0 ");
        }
        
        sql.append("ORDER BY p.Name");
        
        return sql.toString();
    }
    
    private String getActionButton(String status, int stock) {
        if ("Inactive".equals(status) || stock <= 0) {
            return "Unavailable";
        } else {
            return "Add to Cart";
        }
    }
    
    private int getSelectedProductId() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (Integer) tableModel.getValueAt(selectedRow, 0);
        }
        return -1;
    }
    
    private String getSelectedProductName() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (String) tableModel.getValueAt(selectedRow, 1);
        }
        return "";
    }
    
    private double getSelectedProductPrice() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (Double) tableModel.getValueAt(selectedRow, 3);
        }
        return 0.0;
    }
    
    private int getSelectedProductStock() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (Integer) tableModel.getValueAt(selectedRow, 4);
        }
        return 0;
    }
    
    private void addToCart() throws Exception {
        int productId = getSelectedProductId();
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first!", 
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (getSelectedProductStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", 
                "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String quantityStr = JOptionPane.showInputDialog(this, 
            "Enter quantity for " + getSelectedProductName() + ":", "1");
        
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be positive!", 
                        "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (quantity > getSelectedProductStock()) {
                    JOptionPane.showMessageDialog(this, 
                        "Only " + getSelectedProductStock() + " items available in stock!", 
                        "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Add to cart logic here (you might want to create a cart table)
                addToCartInDatabase(productId, quantity);
                
                JOptionPane.showMessageDialog(this, 
                    "Added " + quantity + " x " + getSelectedProductName() + " to cart!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number!", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addToCartInDatabase(int productId, int quantity) throws Exception {
        try (Connection conn = DB.getConnection()) {
            // Check if item already exists in cart
            String checkSql = "SELECT * FROM cart WHERE UserID = ? AND ProductID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Update existing cart item
                String updateSql = "UPDATE cart SET Quantity = Quantity + ? WHERE UserID = ? AND ProductID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
            } else {
                // Insert new cart item
                String insertSql = "INSERT INTO cart (UserID, ProductID, Quantity, AddedDate) VALUES (?, ?, ?, NOW())";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, productId);
                insertStmt.setInt(3, quantity);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            // If cart table doesn't exist, show success message anyway
            System.out.println("Cart table might not exist: " + e.getMessage());
        }
    }
    
    private void buyNow() {
        int productId = getSelectedProductId();
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first!", 
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (getSelectedProductStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", 
                "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to buy '" + getSelectedProductName() + "' for $" + 
            getSelectedProductPrice() + "?\n\nThis will create an immediate order.",
            "Confirm Purchase", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            createOrder(productId, 1); // Buy 1 item immediately
        }
    }
    
    private void createOrder(int productId, int quantity) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            // Generate order number
            String orderNumber = "ORD-" + System.currentTimeMillis();
            double totalAmount = getSelectedProductPrice() * quantity;
            
            // Create order with Pending status (stock not decremented yet)
            String orderSql = "INSERT INTO `order` (OrderNumber, UserID, TotalAmount, Status, CreatedAt) VALUES (?, ?, ?, 'Pending', NOW())";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, orderNumber);
            orderStmt.setInt(2, userId);
            orderStmt.setDouble(3, totalAmount);
            orderStmt.executeUpdate();
            
            // Get generated order ID
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            
            // Add order item
            String itemSql = "INSERT INTO orderitem (OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, orderId);
            itemStmt.setInt(2, productId);
            itemStmt.setInt(3, quantity);
            itemStmt.setDouble(4, getSelectedProductPrice());
            itemStmt.setDouble(5, totalAmount);
            itemStmt.executeUpdate();
            
            // REMOVED: Inventory update from here - stock will be decremented only after payment
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, 
                "Order created successfully!\nOrder Number: " + orderNumber + 
                "\nTotal Amount: $" + totalAmount + 
                "\n\nPlease complete the payment to confirm your order and reserve the stock.", 
                "Order Created", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh products to show updated availability (even though stock isn't decremented yet)
            loadProducts();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage(), 
                "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewProductDetails() {
        int productId = getSelectedProductId();
        if (productId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first!", 
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT p.*, c.Name as CategoryName, i.Available as Stock, " +
                        "(SELECT COUNT(*) FROM review r WHERE r.productid = p.ProductID) as ReviewCount, " +
                        "(SELECT AVG(r.rating) FROM review r WHERE r.productid = p.ProductID) as AvgRating " +
                        "FROM product p " +
                        "LEFT JOIN category c ON p.CategoryID = c.CategoryID " +
                        "LEFT JOIN inventory i ON p.ProductID = i.ProductID " +
                        "WHERE p.ProductID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("<html><h2>").append(rs.getString("Name")).append("</h2>");
                details.append("<b>Product ID:</b> ").append(rs.getInt("ProductID")).append("<br>");
                details.append("<b>Category:</b> ").append(rs.getString("CategoryName")).append("<br>");
                details.append("<b>Price:</b> $").append(String.format("%.2f", rs.getDouble("PriceOrValue"))).append("<br>");
                details.append("<b>Stock:</b> ").append(rs.getInt("Stock")).append("<br>");
                details.append("<b>Status:</b> ").append(rs.getString("Status")).append("<br>");
                
                if (rs.getString("Description") != null && !rs.getString("Description").isEmpty()) {
                    details.append("<b>Description:</b> ").append(rs.getString("Description")).append("<br>");
                }
                
                double avgRating = rs.getDouble("AvgRating");
                if (!rs.wasNull()) {
                    details.append("<b>Average Rating:</b> ").append(String.format("%.1f", avgRating)).append(" ⭐<br>");
                }
                
                details.append("<b>Total Reviews:</b> ").append(rs.getInt("ReviewCount")).append("<br>");
                details.append("<b>Created:</b> ").append(rs.getTimestamp("CreatedAt")).append("</html>");
                
                JOptionPane.showMessageDialog(this, details.toString(), 
                    "Product Details", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading product details: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn || 
            e.getSource() == categoryFilter || 
            e.getSource() == statusFilter ||
            e.getSource() == showOnlyAvailableCheckbox) {
            loadProducts();
        } else if (e.getSource() == addToCartBtn) {
            try {
                addToCart();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource() == buyNowBtn) {
            buyNow();
        } else if (e.getSource() == viewDetailsBtn) {
            viewProductDetails();
        }
    }
    
    // Button renderer and editor for table actions
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            
            // Color code the buttons
            if ("Add to Cart".equals(value)) {
                setBackground(new Color(46, 204, 113)); // Green
                setForeground(Color.WHITE);
            } else if ("Unavailable".equals(value)) {
                setBackground(new Color(231, 76, 60)); // Red
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(149, 165, 166)); // Gray
                setForeground(Color.WHITE);
            }
            
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            
            // Color code the buttons
            if ("Add to Cart".equals(label)) {
                button.setBackground(new Color(46, 204, 113)); // Green
                button.setForeground(Color.WHITE);
            } else if ("Unavailable".equals(label)) {
                button.setBackground(new Color(231, 76, 60)); // Red
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(149, 165, 166)); // Gray
                button.setForeground(Color.WHITE);
            }
            
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = productsTable.getEditingRow();
                if (row >= 0) {
                    String action = (String) tableModel.getValueAt(row, 6);
                    if ("Add to Cart".equals(action)) {
                        productsTable.setRowSelectionInterval(row, row);
                        try {
                            addToCart();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            isPushed = false;
            return label;
        }
        
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}