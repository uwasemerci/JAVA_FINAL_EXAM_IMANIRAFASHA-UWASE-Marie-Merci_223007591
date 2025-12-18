package com.panel;

import com.util.DB;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel implements ActionListener {
    
    // UI Components
    private JTable ordersTable;
    private JTable orderItemsTable;
    private DefaultTableModel ordersTableModel;
    private DefaultTableModel orderItemsTableModel;
    
    private JButton refreshBtn, viewDetailsBtn, updateStatusBtn, deleteBtn;
    private JButton createOrderBtn, cancelOrderBtn, printInvoiceBtn;
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;
    private JLabel statsLabel;
    
    // User information
    private final int userId;
    private final String userRole;
    
    // Modern UI Colors
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(139, 92, 246);
    private static final Color LIGHT_BG = new Color(249, 250, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    
    // Status colors
    private static final Color STATUS_PENDING = new Color(245, 158, 11);
    private static final Color STATUS_CONFIRMED = new Color(59, 130, 246);
    private static final Color STATUS_SHIPPED = new Color(139, 92, 246);
    private static final Color STATUS_DELIVERED = new Color(34, 197, 94);
    private static final Color STATUS_CANCELLED = new Color(239, 68, 68);
    
    public OrderPanel(int userId, String userRole) throws Exception {
        this.userId = userId;
        this.userRole = userRole != null ? userRole.toLowerCase() : "buyer";
        
        validateUserRole();
        initializePanel();
        loadOrders();
    }
    
    private void validateUserRole() throws Exception {
        if (!userRole.equals("admin") && !userRole.equals("seller") && !userRole.equals("buyer") && !userRole.equals("supplier")) {
            throw new Exception("Invalid user role: " + userRole);
        }
    }
    
    private void initializePanel() throws Exception {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() throws Exception {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        String subtitleText = getSubtitleText();
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Stats section
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(CARD_BG);
        
        statsLabel = new JLabel("Loading orders...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);
        
        statsPanel.add(statsLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private String getSubtitleText() {
        switch (userRole) {
            case "admin": return "Manage all orders across the system";
            case "seller": return "Manage orders for your products";
            case "supplier": return "View and fulfill orders for your products";
            case "buyer": return "View and manage your purchase orders";
            default: return "Order management";
        }
    }
    
    private JPanel createMainPanel() throws Exception {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(LIGHT_BG);
        
        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createFilterPanel() throws Exception {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Search field
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Search by order number or customer name");
        searchField.addActionListener(e -> {
			try {
				loadOrders();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        filterPanel.add(searchField);
        
        // Status filter
        filterPanel.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{
            "All Status", "Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"
        });
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilterCombo.addActionListener(e -> {
			try {
				loadOrders();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        filterPanel.add(statusFilterCombo);
        
        // Refresh button
        refreshBtn = createStyledButton("Refresh", PRIMARY_COLOR, "Reload orders");
        refreshBtn.addActionListener(this);
        filterPanel.add(refreshBtn);
        
        return filterPanel;
    }
    
    private JPanel createContentPanel() throws Exception {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(LIGHT_BG);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);
        
        // Orders panel
        JPanel ordersPanel = createCardPanel("Orders List", createOrdersTable());
        splitPane.setTopComponent(ordersPanel);
        
        // Order items panel
        JPanel itemsPanel = createCardPanel("Order Items & Product Status", createOrderItemsTable());
        splitPane.setBottomComponent(itemsPanel);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        return contentPanel;
    }
    
    private JPanel createCardPanel(String title, Component content) throws Exception {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JScrollPane createOrdersTable() throws Exception {
        String[] columns = {"Order ID", "Order Number", "Customer", "Total Amount", "Status", "Order Date", "Payment"};
        ordersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersTable = new JTable(ordersTableModel);
        styleTable(ordersTable);
        
        // Set column renderers for better display
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        // Add selection listener
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow != -1) {
                    try {
                        String orderIdStr = ordersTableModel.getValueAt(selectedRow, 0).toString();
                        long orderId = Long.parseLong(orderIdStr);
                        loadOrderItems(orderId);
                    } catch (Exception ex) {
                        showError("Error loading order items: " + ex.getMessage());
                    }
                }
                updateButtonStates();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return scrollPane;
    }
    
    private JScrollPane createOrderItemsTable() throws Exception {
        String[] columns = {"Product Name", "Quantity", "Unit Price", "Total Price", "Product Status", "Inventory"};
        orderItemsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        orderItemsTable = new JTable(orderItemsTableModel);
        styleTable(orderItemsTable);
        
        // Set column renderers
        orderItemsTable.getColumnModel().getColumn(4).setCellRenderer(new ProductStatusCellRenderer());
        orderItemsTable.getColumnModel().getColumn(5).setCellRenderer(new InventoryStatusCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(orderItemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return scrollPane;
    }
    
    private JPanel createFooterPanel() throws Exception {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(LIGHT_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(LIGHT_BG);
        
        // Common buttons
        viewDetailsBtn = createStyledButton("View Details", INFO_COLOR, "View complete order details");
        viewDetailsBtn.addActionListener(this);
        actionPanel.add(viewDetailsBtn);
        
        // Role-specific buttons
        if (userRole.equals("admin") || userRole.equals("seller") || userRole.equals("supplier")) {
            updateStatusBtn = createStyledButton("Update Status", PRIMARY_COLOR, "Update order status");
            updateStatusBtn.addActionListener(this);
            actionPanel.add(updateStatusBtn);
            
            deleteBtn = createStyledButton("Delete Order", DANGER_COLOR, "Delete selected order (Admin only)");
            deleteBtn.addActionListener(this);
            actionPanel.add(deleteBtn);
        }
        
        if (userRole.equals("buyer")) {
            createOrderBtn = createStyledButton("Create Order", SUCCESS_COLOR, "Create a new order");
            createOrderBtn.addActionListener(this);
            actionPanel.add(createOrderBtn);
            
            cancelOrderBtn = createStyledButton("Cancel Order", WARNING_COLOR, "Cancel selected order");
            cancelOrderBtn.addActionListener(this);
            actionPanel.add(cancelOrderBtn);
        }
        
        // Print invoice button
        printInvoiceBtn = createStyledButton("Print Invoice", SUCCESS_COLOR, "Generate invoice for selected order");
        printInvoiceBtn.addActionListener(this);
        actionPanel.add(printInvoiceBtn);
        
        footer.add(actionPanel, BorderLayout.WEST);
        
        updateButtonStates();
        return footer;
    }
    
    private JButton createStyledButton(String text, Color color, String tooltip) throws Exception {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void styleTable(JTable table) throws Exception {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(59, 130, 246, 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }
    
    // Custom cell renderer for order status
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                
                switch (status) {
                    case "Pending":
                        setBackground(STATUS_PENDING);
                        setForeground(Color.WHITE);
                        break;
                    case "Confirmed":
                        setBackground(STATUS_CONFIRMED);
                        setForeground(Color.WHITE);
                        break;
                    case "Shipped":
                        setBackground(STATUS_SHIPPED);
                        setForeground(Color.WHITE);
                        break;
                    case "Delivered":
                        setBackground(STATUS_DELIVERED);
                        setForeground(Color.WHITE);
                        break;
                    case "Cancelled":
                        setBackground(STATUS_CANCELLED);
                        setForeground(Color.WHITE);
                        break;
                    default:
                        setBackground(Color.LIGHT_GRAY);
                        setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
            }
            
            return c;
        }
    }
    
    // Custom cell renderer for product status
    private class ProductStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                
                if ("Active".equals(status)) {
                    setBackground(SUCCESS_COLOR);
                    setForeground(Color.WHITE);
                } else if ("Inactive".equals(status)) {
                    setBackground(DANGER_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.LIGHT_GRAY);
                    setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
            }
            
            return c;
        }
    }
    
    // Custom cell renderer for inventory status
    private class InventoryStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String inventory = value.toString();
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                
                try {
                    int available = Integer.parseInt(inventory);
                    if (available > 10) {
                        setBackground(SUCCESS_COLOR);
                        setForeground(Color.WHITE);
                    } else if (available > 0) {
                        setBackground(WARNING_COLOR);
                        setForeground(Color.WHITE);
                    } else {
                        setBackground(DANGER_COLOR);
                        setForeground(Color.WHITE);
                    }
                } catch (NumberFormatException e) {
                    setBackground(Color.LIGHT_GRAY);
                    setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
            }
            
            return c;
        }
    }
    
    public void loadOrders() throws Exception {
        ordersTableModel.setRowCount(0);
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                o.OrderID, 
                o.OrderNumber, 
                u.FullName AS CustomerName, 
                o.TotalAmount, 
                o.Status AS OrderStatus, 
                o.Date AS OrderDate, 
                o.PaymentMethod,
                COUNT(oi.OrderItemID) AS ItemCount
            FROM `Order` o
            JOIN User u ON o.UserID = u.UserID
            LEFT JOIN OrderItem oi ON o.OrderID = oi.OrderID
            """);
        
        List<Object> params = new ArrayList<>();
        
        // Apply role-based filters
        if (userRole.equals("buyer")) {
            sql.append(" WHERE o.UserID = ?");
            params.add(userId);
        } else if (userRole.equals("seller") || userRole.equals("supplier")) {
            sql.append("""
                WHERE o.OrderID IN (
                    SELECT DISTINCT oi.OrderID 
                    FROM OrderItem oi 
                    JOIN Product p ON oi.ProductID = p.ProductID 
                    JOIN UserProductAccess upa ON p.ProductID = upa.ProductID 
                    WHERE upa.UserID = ? AND upa.AccessRole IN ('Owner', 'Editor')
                )
                """);
            params.add(userId);
        } else {
            // Admin can see all orders
            sql.append(" WHERE 1=1");
        }
        
        // Apply status filter
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        if (selectedStatus != null && !selectedStatus.equals("All Status")) {
            sql.append(" AND o.Status = ?");
            params.add(selectedStatus);
        }
        
        // Apply search filter
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            sql.append(" AND (o.OrderNumber LIKE ? OR u.FullName LIKE ?)");
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }
        
        sql.append(" GROUP BY o.OrderID, o.OrderNumber, u.FullName, o.TotalAmount, o.Status, o.Date, o.PaymentMethod");
        sql.append(" ORDER BY o.OrderID DESC");
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                int totalOrders = 0;
                BigDecimal totalRevenue = BigDecimal.ZERO;
                
                while (rs.next()) {
                    totalOrders++;
                    totalRevenue = totalRevenue.add(rs.getBigDecimal("TotalAmount"));
                    
                    ordersTableModel.addRow(new Object[]{
                        rs.getLong("OrderID"),
                        rs.getString("OrderNumber"),
                        rs.getString("CustomerName"),
                        formatCurrency(rs.getBigDecimal("TotalAmount")),
                        rs.getString("OrderStatus"),
                        formatDate(rs.getTimestamp("OrderDate")),
                        rs.getString("PaymentMethod")
                    });
                }
                
                updateStats(totalOrders, totalRevenue);
            }
        }
        
        updateButtonStates();
    }
    
    private void loadOrderItems(Long orderId) throws Exception {
        orderItemsTableModel.setRowCount(0);
        
        String sql = """
            SELECT 
                oi.OrderItemID,
                p.Name AS ProductName,
                p.ProductID,
                oi.Quantity,
                oi.UnitPrice,
                oi.TotalPrice,
                p.Status AS ProductStatus,
                COALESCE(i.Available, 0) AS InventoryAvailable,
                COALESCE(i.Reserved, 0) AS InventoryReserved
            FROM OrderItem oi
            JOIN Product p ON oi.ProductID = p.ProductID
            LEFT JOIN Inventory i ON p.ProductID = i.ProductID
            WHERE oi.OrderID = ?
            ORDER BY oi.OrderItemID
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productStatus = rs.getString("ProductStatus");
                    int available = rs.getInt("InventoryAvailable");
                    int reserved = rs.getInt("InventoryReserved");
                    
                    String inventoryStatus = String.format("Available: %d, Reserved: %d", available, reserved);
                    
                    orderItemsTableModel.addRow(new Object[]{
                        rs.getString("ProductName"),
                        rs.getInt("Quantity"),
                        formatCurrency(rs.getBigDecimal("UnitPrice")),
                        formatCurrency(rs.getBigDecimal("TotalPrice")),
                        productStatus,
                        inventoryStatus
                    });
                }
            }
        }
    }
    
    private void updateStats(int totalOrders, BigDecimal totalRevenue) {
        String statsText = String.format("%,d orders | Total Revenue: RWF %,.2f", totalOrders, totalRevenue);
        statsLabel.setText(statsText);
    }
    
    private void updateButtonStates() {
        boolean hasSelection = ordersTable.getSelectedRow() != -1;
        
        viewDetailsBtn.setEnabled(hasSelection);
        printInvoiceBtn.setEnabled(hasSelection);
        
        if (updateStatusBtn != null) {
            updateStatusBtn.setEnabled(hasSelection);
        }
        
        if (deleteBtn != null) {
            deleteBtn.setEnabled(hasSelection && userRole.equals("admin"));
        }
        
        if (cancelOrderBtn != null) {
            boolean canCancel = hasSelection && canCancelOrder();
            cancelOrderBtn.setEnabled(canCancel);
        }
    }
    
    private boolean canCancelOrder() {
        int row = ordersTable.getSelectedRow();
        if (row == -1) return false;
        
        String status = ordersTableModel.getValueAt(row, 4).toString();
        return "Pending".equals(status) || "Confirmed".equals(status);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();
            
            if (source == refreshBtn) {
                loadOrders();
            } else if (source == viewDetailsBtn) {
                viewOrderDetails();
            } else if (source == updateStatusBtn) {
                updateOrderStatus();
            } else if (source == deleteBtn) {
                deleteOrder();
            } else if (source == cancelOrderBtn) {
                cancelOrder();
            } else if (source == printInvoiceBtn) {
                printInvoice();
            } else if (source == createOrderBtn) {
                createNewOrder();
            }
        } catch (Exception ex) {
            showError("Operation failed: " + ex.getMessage());
        }
    }
    
    private void viewOrderDetails() throws Exception {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            throw new Exception("Please select an order to view details.");
        }
        
        Long orderId = (Long) ordersTableModel.getValueAt(row, 0);
        String orderNumber = (String) ordersTableModel.getValueAt(row, 1);
        
        // Get detailed order information
        String orderDetails = getOrderDetails(orderId);
        
        JTextArea textArea = new JTextArea(orderDetails);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Order Details - " + orderNumber, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String getOrderDetails(Long orderId) throws Exception {
        StringBuilder details = new StringBuilder();
        
        try (Connection conn = DB.getConnection()) {
            // Get order header information
            String orderSql = """
                SELECT 
                    o.OrderNumber, o.TotalAmount, o.Status, o.Date, o.PaymentMethod, o.Notes,
                    u.FullName AS CustomerName, u.Email, u.FullName,
                    s.TrackingNumber, s.Carrier, s.Status AS ShipmentStatus
                FROM `Order` o
                JOIN User u ON o.UserID = u.UserID
                LEFT JOIN Shipment s ON o.OrderID = s.OrderID
                WHERE o.OrderID = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setLong(1, orderId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        details.append("ORDER DETAILS\n");
                        details.append("=============\n\n");
                        details.append("Order Number: ").append(rs.getString("OrderNumber")).append("\n");
                        details.append("Customer: ").append(rs.getString("CustomerName")).append("\n");
                        details.append("Email: ").append(rs.getString("Email")).append("\n");
                        details.append("Order Date: ").append(formatDate(rs.getTimestamp("Date"))).append("\n");
                        details.append("Status: ").append(rs.getString("Status")).append("\n");
                        details.append("Total Amount: ").append(formatCurrency(rs.getBigDecimal("TotalAmount"))).append("\n");
                        details.append("Payment Method: ").append(rs.getString("PaymentMethod")).append("\n");
                        
                        if (rs.getString("TrackingNumber") != null) {
                            details.append("Tracking: ").append(rs.getString("TrackingNumber")).append("\n");
                            details.append("Carrier: ").append(rs.getString("Carrier")).append("\n");
                            details.append("Shipment Status: ").append(rs.getString("ShipmentStatus")).append("\n");
                        }
                        
                        if (rs.getString("Notes") != null) {
                            details.append("Notes: ").append(rs.getString("Notes")).append("\n");
                        }
                    }
                }
            }
            
            details.append("\nORDER ITEMS\n");
            details.append("===========\n\n");
            
            // Get order items with product status
            String itemsSql = """
                SELECT 
                    p.Name AS ProductName,
                    p.ProductID,
                    p.Status AS ProductStatus,
                    oi.Quantity,
                    oi.UnitPrice,
                    oi.TotalPrice,
                    COALESCE(i.Available, 0) AS InventoryAvailable,
                    COALESCE(i.Reserved, 0) AS InventoryReserved
                FROM OrderItem oi
                JOIN Product p ON oi.ProductID = p.ProductID
                LEFT JOIN Inventory i ON p.ProductID = i.ProductID
                WHERE oi.OrderID = ?
                ORDER BY oi.OrderItemID
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                stmt.setLong(1, orderId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    int itemCount = 0;
                    BigDecimal orderTotal = BigDecimal.ZERO;
                    
                    while (rs.next()) {
                        itemCount++;
                        String productName = rs.getString("ProductName");
                        String productStatus = rs.getString("ProductStatus");
                        int quantity = rs.getInt("Quantity");
                        BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");
                        BigDecimal totalPrice = rs.getBigDecimal("TotalPrice");
                        int available = rs.getInt("InventoryAvailable");
                        int reserved = rs.getInt("InventoryReserved");
                        
                        orderTotal = orderTotal.add(totalPrice);
                        
                        details.append(String.format("%d. %s\n", itemCount, productName));
                        details.append(String.format("   Quantity: %d | Unit Price: %s | Total: %s\n", 
                            quantity, formatCurrency(unitPrice), formatCurrency(totalPrice)));
                        details.append(String.format("   Product Status: %s | Inventory: Available=%d, Reserved=%d\n", 
                            productStatus, available, reserved));
                        details.append("\n");
                    }
                    
                    details.append(String.format("ORDER TOTAL: %s\n", formatCurrency(orderTotal)));
                }
            }
        }
        
        return details.toString();
    }
    
    private void updateOrderStatus() throws Exception {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            throw new Exception("Please select an order to update status.");
        }
        
        Long orderId = (Long) ordersTableModel.getValueAt(row, 0);
        String currentStatus = ordersTableModel.getValueAt(row, 4).toString();
        String orderNumber = (String) ordersTableModel.getValueAt(row, 1);
        
        String[] statusOptions = {"Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
            "Order: " + orderNumber + "\n" +
            "Current Status: " + currentStatus + "\n\n" +
            "Select new status:",
            "Update Order Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusOptions,
            currentStatus);
        
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            try (Connection conn = DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE `Order` SET Status = ? WHERE OrderID = ?")) {
                
                stmt.setString(1, newStatus);
                stmt.setLong(2, orderId);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    showSuccess("Order status updated from '" + currentStatus + "' to '" + newStatus + "'");
                    loadOrders();
                    
                    // If status is Shipped, create shipment record
                    if ("Shipped".equals(newStatus)) {
                        createShipmentRecord(orderId, orderNumber);
                    }
                } else {
                    throw new Exception("Failed to update order status.");
                }
            }
        }
    }
    
    private void createShipmentRecord(Long orderId, String orderNumber) throws Exception {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO Shipment (OrderID, TrackingNumber, Carrier, Status) VALUES (?, ?, ?, 'Shipped')")) {
            
            String trackingNumber = "TRK-" + System.currentTimeMillis();
            String carrier = "Standard Delivery";
            
            stmt.setLong(1, orderId);
            stmt.setString(2, trackingNumber);
            stmt.setString(3, carrier);
            
            stmt.executeUpdate();
            showInfo("Shipment created with tracking number: " + trackingNumber);
            
        } catch (SQLException ex) {
            showWarning("Order status updated but failed to create shipment record: " + ex.getMessage());
        }
    }
    
    private void deleteOrder() throws Exception {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            throw new Exception("Please select an order to delete.");
        }
        
        Long orderId = (Long) ordersTableModel.getValueAt(row, 0);
        String orderNumber = (String) ordersTableModel.getValueAt(row, 1);
        
        if (!userRole.equals("admin")) {
            throw new Exception("Only administrators can delete orders.");
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "WARNING: This will permanently delete the order and all associated data!\n\n" +
            "Order: " + orderNumber + "\n" +
            "This action cannot be undone!\n\n" +
            "Are you sure you want to delete this order?",
            "Confirm Order Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DB.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Delete related records first
                    deleteRelatedRecords(conn, orderId);
                    
                    // Delete the order
                    try (PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM `Order` WHERE OrderID = ?")) {
                        stmt.setLong(1, orderId);
                        int affectedRows = stmt.executeUpdate();
                        
                        if (affectedRows > 0) {
                            conn.commit();
                            showSuccess("Order deleted successfully: " + orderNumber);
                            loadOrders();
                        } else {
                            conn.rollback();
                            throw new Exception("Order not found or already deleted.");
                        }
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw new Exception("Database error: " + ex.getMessage());
                }
            }
        }
    }
    
    private void deleteRelatedRecords(Connection conn, Long orderId) throws SQLException {
        // Delete from OrderPayment
        try (PreparedStatement stmt = conn.prepareStatement(
             "DELETE FROM OrderPayment WHERE OrderID = ?")) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
        
        // Delete from Shipment
        try (PreparedStatement stmt = conn.prepareStatement(
             "DELETE FROM Shipment WHERE OrderID = ?")) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
        
        // Delete from OrderItem
        try (PreparedStatement stmt = conn.prepareStatement(
             "DELETE FROM OrderItem WHERE OrderID = ?")) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }
    
    private void cancelOrder() throws Exception {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            throw new Exception("Please select an order to cancel.");
        }
        
        if (!canCancelOrder()) {
            throw new Exception("This order cannot be cancelled. Only Pending or Confirmed orders can be cancelled.");
        }
        
        Long orderId = (Long) ordersTableModel.getValueAt(row, 0);
        String orderNumber = (String) ordersTableModel.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel order:\n" +
            orderNumber + "?",
            "Confirm Order Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE `Order` SET Status = 'Cancelled' WHERE OrderID = ?")) {
                
                stmt.setLong(1, orderId);
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    showSuccess("Order cancelled successfully: " + orderNumber);
                    loadOrders();
                } else {
                    throw new Exception("Failed to cancel order.");
                }
            }
        }
    }
    
    private void printInvoice() throws Exception {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            throw new Exception("Please select an order to generate invoice.");
        }
        
        Long orderId = (Long) ordersTableModel.getValueAt(row, 0);
        String orderNumber = (String) ordersTableModel.getValueAt(row, 1);
        
        String invoice = generateInvoice(orderId);
        
        JTextArea textArea = new JTextArea(invoice);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Invoice - " + orderNumber, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String generateInvoice(Long orderId) throws Exception {
        StringBuilder invoice = new StringBuilder();
        
        try (Connection conn = DB.getConnection()) {
            // Get order details
            String orderSql = """
                SELECT o.OrderNumber, o.TotalAmount, o.Status, o.Date, o.PaymentMethod,
                       u.FullName AS CustomerName, u.Email, u.FullName
                FROM `Order` o
                JOIN User u ON o.UserID = u.UserID
                WHERE o.OrderID = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setLong(1, orderId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        invoice.append("============================================\n");
                        invoice.append("               INVOICE\n");
                        invoice.append("============================================\n\n");
                        invoice.append("Order Number: ").append(rs.getString("OrderNumber")).append("\n");
                        invoice.append("Customer: ").append(rs.getString("CustomerName")).append("\n");
                        invoice.append("Email: ").append(rs.getString("Email")).append("\n");
                        invoice.append("Order Date: ").append(formatDate(rs.getTimestamp("Date"))).append("\n");
                        invoice.append("Status: ").append(rs.getString("Status")).append("\n");
                        invoice.append("Payment Method: ").append(rs.getString("PaymentMethod")).append("\n\n");
                        
                        invoice.append("--------------------------------------------\n");
                        invoice.append("Items Ordered:\n");
                        invoice.append("--------------------------------------------\n");
                    }
                }
            }
            
            // Get order items with product status
            String itemsSql = """
                SELECT p.Name, oi.Quantity, oi.UnitPrice, oi.TotalPrice, p.Status AS ProductStatus
                FROM OrderItem oi
                JOIN Product p ON oi.ProductID = p.ProductID
                WHERE oi.OrderID = ?
                ORDER BY oi.OrderItemID
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                stmt.setLong(1, orderId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    BigDecimal total = BigDecimal.ZERO;
                    int itemNumber = 1;
                    
                    while (rs.next()) {
                        String productName = rs.getString("Name");
                        int quantity = rs.getInt("Quantity");
                        BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");
                        BigDecimal itemTotal = rs.getBigDecimal("TotalPrice");
                        String productStatus = rs.getString("ProductStatus");
                        
                        total = total.add(itemTotal);
                        
                        invoice.append(String.format("%2d. %-25s\n", itemNumber, 
                            productName.length() > 25 ? productName.substring(0, 22) + "..." : productName));
                        invoice.append(String.format("    Qty: %3d  Price: %8s  Total: %8s\n", 
                            quantity, formatCurrency(unitPrice), formatCurrency(itemTotal)));
                        invoice.append(String.format("    Status: %s\n", productStatus));
                        invoice.append("\n");
                        itemNumber++;
                    }
                    
                    invoice.append("--------------------------------------------\n");
                    invoice.append(String.format("TOTAL AMOUNT: %26s\n", formatCurrency(total)));
                    invoice.append("============================================\n");
                    invoice.append("\nThank you for your business!\n");
                }
            }
        }
        
        return invoice.toString();
    }
    
    private void createNewOrder() throws Exception {
        // This would integrate with the ProductsPanel for order creation
        JOptionPane.showMessageDialog(this,
            "Order creation would open the product catalog.\n\n" +
            "From there you can:\n" +
            "1. Browse available products (Active status only)\n" +
            "2. Check product inventory availability\n" +
            "3. Add items to your order\n" +
            "4. Review order summary\n" +
            "5. Place the order with payment method selection",
            "Create New Order",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Utility methods
    private String formatCurrency(BigDecimal amount) {
        return amount == null ? "RWF 0.00" : String.format("RWF %,.2f", amount);
    }
    
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}