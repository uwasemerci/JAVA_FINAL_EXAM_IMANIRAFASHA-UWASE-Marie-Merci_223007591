package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class Inventorypanel extends JPanel implements ActionListener {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, updateBtn, viewLogBtn, addProductBtn;
    
    // Modern Color Scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(231, 76, 60);
    private final Color INFO_COLOR = new Color(155, 89, 182);
    private final Color BACKGROUND_COLOR = new Color(248, 248, 248);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);
    private final Color TEXT_SECONDARY = new Color(119, 119, 119);
    
    // Statistics labels
    private JLabel totalProductsLabel, lowStockLabel, outOfStockLabel;

    public Inventorypanel() {
        initializePanel();
        loadInventoryData();
        updateStatistics();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // Title Section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Manage product stock levels and track inventory changes");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Stats Section
        JPanel statsPanel = createStatsPanel();
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setBackground(CARD_COLOR);
        
        // Create stat cards with placeholder values
        JPanel totalCard = createStatCard("Total Products", "0", PRIMARY_COLOR);
        JPanel lowStockCard = createStatCard("Low Stock", "0", WARNING_COLOR);
        JPanel outOfStockCard = createStatCard("Out of Stock", "0", new Color(231, 76, 60));
        
        // Get references to the value labels for updating
        totalProductsLabel = extractValueLabel(totalCard);
        lowStockLabel = extractValueLabel(lowStockCard);
        outOfStockLabel = extractValueLabel(outOfStockCard);
        
        statsPanel.add(totalCard);
        statsPanel.add(lowStockCard);
        statsPanel.add(outOfStockCard);

        return statsPanel;
    }

    private JLabel extractValueLabel(JPanel card) {
        // Extract the value label from the card component structure
        Component[] components = card.getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            return (JLabel) components[0];
        }
        // Create a new label if extraction fails
        return new JLabel("0");
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setPreferredSize(new Dimension(120, 70));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(titleLabel);

        return card;
    }

    private void updateStatistics() {
        try (Connection conn = DB.getConnection()) {
            // Count total products in inventory
            String totalSql = "SELECT COUNT(*) as total FROM Inventory i INNER JOIN Product p ON i.ProductID = p.ProductID WHERE p.Status = 'Active'";
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            ResultSet totalRs = totalStmt.executeQuery();
            
            int totalProducts = 0;
            int lowStock = 0;
            int outOfStock = 0;
            
            if (totalRs.next()) {
                totalProducts = totalRs.getInt("total");
            }
            
            // Count low stock (available < 10 but > 0)
            String lowStockSql = "SELECT COUNT(*) as lowStock FROM Inventory i INNER JOIN Product p ON i.ProductID = p.ProductID WHERE p.Status = 'Active' AND i.Available > 0 AND i.Available < 10";
            PreparedStatement lowStockStmt = conn.prepareStatement(lowStockSql);
            ResultSet lowStockRs = lowStockStmt.executeQuery();
            
            if (lowStockRs.next()) {
                lowStock = lowStockRs.getInt("lowStock");
            }
            
            // Count out of stock (available = 0)
            String outOfStockSql = "SELECT COUNT(*) as outOfStock FROM Inventory i INNER JOIN Product p ON i.ProductID = p.ProductID WHERE p.Status = 'Active' AND i.Available <= 0";
            PreparedStatement outOfStockStmt = conn.prepareStatement(outOfStockSql);
            ResultSet outOfStockRs = outOfStockStmt.executeQuery();
            
            if (outOfStockRs.next()) {
                outOfStock = outOfStockRs.getInt("outOfStock");
            }
            
            // Update labels on EDT
            final int finalTotalProducts = totalProducts;
            final int finalLowStock = lowStock;
            final int finalOutOfStock = outOfStock;
            
            SwingUtilities.invokeLater(() -> {
                totalProductsLabel.setText(String.valueOf(finalTotalProducts));
                lowStockLabel.setText(String.valueOf(finalLowStock));
                outOfStockLabel.setText(String.valueOf(finalOutOfStock));
            });
            
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            // Set default values on error
            SwingUtilities.invokeLater(() -> {
                totalProductsLabel.setText("0");
                lowStockLabel.setText("0");
                outOfStockLabel.setText("0");
            });
        }
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Table setup
        String[] columns = {"Product ID", "Product Name", "Category", "Quantity", "Reserved", "Available", "Status", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.setRowHeight(35);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inventoryTable.setShowGrid(false);
        inventoryTable.setIntercellSpacing(new Dimension(0, 0));

        // Style table header
        JTableHeader header = inventoryTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create styled buttons
        refreshBtn = createStyledButton("Refresh", PRIMARY_COLOR, "Reload inventory data");
        updateBtn = createStyledButton("Update Stock", SUCCESS_COLOR, "Update stock levels for selected product");
        viewLogBtn = createStyledButton("View Log", INFO_COLOR, "View inventory change history");
        addProductBtn = createStyledButton("Add Product", new Color(243, 156, 18), "Add new product to inventory");

        refreshBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        viewLogBtn.addActionListener(this);
        addProductBtn.addActionListener(this);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(viewLogBtn);
        buttonPanel.add(addProductBtn);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker()),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private void loadInventoryData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT " +
                        "i.InventoryID, p.ProductID, p.Name as ProductName, " +
                        "c.Name as CategoryName, i.Quantity, i.Reserved, i.Available, " +
                        "CASE " +
                        "    WHEN i.Available <= 0 THEN 'Out of Stock' " +
                        "    WHEN i.Available < 10 THEN 'Low Stock' " +
                        "    ELSE 'In Stock' " +
                        "END as StockStatus, " +
                        "i.LastUpdated " +
                        "FROM Inventory i " +
                        "INNER JOIN Product p ON i.ProductID = p.ProductID " +
                        "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
                        "ORDER BY i.Available ASC, p.Name ASC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("ProductID"),
                    rs.getString("ProductName"),
                    rs.getString("CategoryName"),
                    rs.getInt("Quantity"),
                    rs.getInt("Reserved"),
                    rs.getInt("Available"),
                    rs.getString("StockStatus"),
                    rs.getTimestamp("LastUpdated")
                };
                tableModel.addRow(row);
            }
            
            // Update statistics after loading data
            updateStatistics();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading inventory data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadInventoryData();
        } else if (e.getSource() == updateBtn) {
            updateStock();
        } else if (e.getSource() == viewLogBtn) {
            viewInventoryLog();
        } else if (e.getSource() == addProductBtn) {
            addProductToInventory();
        }
    }

    private void updateStock() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);
            int currentStock = (int) tableModel.getValueAt(selectedRow, 3);
            
            showStockUpdateDialog(productId, productName, currentStock);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to update stock.", 
                "Selection Required", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showStockUpdateDialog(int productId, String productName, int currentStock) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel productLabel = new JLabel("Product:");
        JLabel productValue = new JLabel(productName);
        productValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel currentLabel = new JLabel("Current Stock:");
        JLabel currentValue = new JLabel(String.valueOf(currentStock));
        currentValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel changeLabel = new JLabel("Quantity Change:");
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
        
        JLabel typeLabel = new JLabel("Change Type:");
        JComboBox<String> changeTypeCombo = new JComboBox<>(
            new String[]{"Purchase", "Sale", "Adjustment", "Return", "Damage", "Transfer"}
        );
        
        panel.add(productLabel);
        panel.add(productValue);
        panel.add(currentLabel);
        panel.add(currentValue);
        panel.add(changeLabel);
        panel.add(quantitySpinner);
        panel.add(typeLabel);
        panel.add(changeTypeCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Update Stock", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            int quantityChange = (int) quantitySpinner.getValue();
            String changeType = (String) changeTypeCombo.getSelectedItem();
            
            if (quantityChange != 0) {
                updateInventoryInDB(productId, changeType, quantityChange);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Quantity change cannot be zero.", 
                    "Invalid Input", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void updateInventoryInDB(int productId, String changeType, int quantityChange) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get current inventory values
            String selectSql = "SELECT Quantity, Available FROM Inventory WHERE ProductID = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, productId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                int previousQty = rs.getInt("Quantity");
                int previousAvailable = rs.getInt("Available");
                int newQty = previousQty + quantityChange;
                int newAvailable = previousAvailable + quantityChange;
                
                // Prevent negative stock
                if (newAvailable < 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot set stock to negative value.", 
                        "Invalid Operation", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update inventory
                String updateSql = "UPDATE Inventory SET Quantity = ?, Available = ?, LastUpdated = NOW() WHERE ProductID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, newQty);
                updateStmt.setInt(2, newAvailable);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
                
                // Log the change in InventoryLog
                String logSql = "INSERT INTO InventoryLog (InventoryID, ProductID, ChangeType, QuantityChange, PreviousQty, NewQty, ReferenceType) " +
                              "SELECT i.InventoryID, i.ProductID, ?, ?, ?, ?, 'Manual Adjustment' " +
                              "FROM Inventory i WHERE i.ProductID = ?";
                PreparedStatement logStmt = conn.prepareStatement(logSql);
                logStmt.setString(1, changeType);
                logStmt.setInt(2, quantityChange);
                logStmt.setInt(3, previousQty);
                logStmt.setInt(4, newQty);
                logStmt.setInt(5, productId);
                logStmt.executeUpdate();
                
                conn.commit();
                
                // Update product status if needed
                updateProductStatus(conn, productId, newAvailable);
                
                // Reload data and statistics
                loadInventoryData();
                
                JOptionPane.showMessageDialog(this, 
                    "Stock updated successfully!\n\n" +
                    "Product: " + productId + "\n" +
                    "Change: " + (quantityChange > 0 ? "+" : "") + quantityChange + "\n" +
                    "Type: " + changeType + "\n" +
                    "New Stock Level: " + newQty, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Product not found in inventory.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating inventory: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateProductStatus(Connection conn, int productId, int availableQty) throws SQLException {
        String status = availableQty > 0 ? "Active" : "Inactive";
        String updateSql = "UPDATE Product SET Status = ? WHERE ProductID = ?";
        PreparedStatement stmt = conn.prepareStatement(updateSql);
        stmt.setString(1, status);
        stmt.setInt(2, productId);
        stmt.executeUpdate();
    }

    private void addProductToInventory() {
        try (Connection conn = DB.getConnection()) {
            // Get products not in inventory
            String sql = "SELECT p.ProductID, p.Name, c.Name as CategoryName " +
                        "FROM Product p " +
                        "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
                        "WHERE p.ProductID NOT IN (SELECT ProductID FROM Inventory) " +
                        "AND p.Status = 'Active'";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            DefaultTableModel productModel = new DefaultTableModel(new String[]{"Product ID", "Product Name", "Category"}, 0);
            while (rs.next()) {
                productModel.addRow(new Object[]{
                    rs.getInt("ProductID"),
                    rs.getString("Name"),
                    rs.getString("CategoryName")
                });
            }
            
            if (productModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "All active products are already in inventory.", 
                    "No Products Available", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JTable productTable = new JTable(productModel);
            productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JScrollPane scrollPane = new JScrollPane(productTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            
            JSpinner initialStockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
            
            Object[] message = {
                "Select product to add to inventory:",
                scrollPane,
                "Initial Stock Quantity:",
                initialStockSpinner
            };
            
            int result = JOptionPane.showConfirmDialog(this, message, 
                    "Add Product to Inventory", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION && productTable.getSelectedRow() >= 0) {
                int selectedRow = productTable.getSelectedRow();
                int productId = (int) productModel.getValueAt(selectedRow, 0);
                String productName = (String) productModel.getValueAt(selectedRow, 1);
                int initialStock = (int) initialStockSpinner.getValue();
                
                addProductToInventoryDB(productId, initialStock);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading products: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductToInventoryDB(int productId, int initialStock) {
        try (Connection conn = DB.getConnection()) {
            // Insert into Inventory table
            String sql = "INSERT INTO Inventory (ProductID, Quantity, Reserved, Available, LastUpdated) VALUES (?, ?, 0, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            stmt.setInt(2, initialStock);
            stmt.setInt(3, initialStock);
            stmt.executeUpdate();
            
            // Log the initial addition
            String logSql = "INSERT INTO InventoryLog (InventoryID, ProductID, ChangeType, QuantityChange, PreviousQty, NewQty, ReferenceType) " +
                          "SELECT i.InventoryID, i.ProductID, 'Initial Stock', ?, 0, ?, 'System' " +
                          "FROM Inventory i WHERE i.ProductID = ?";
            PreparedStatement logStmt = conn.prepareStatement(logSql);
            logStmt.setInt(1, initialStock);
            logStmt.setInt(2, initialStock);
            logStmt.setInt(3, productId);
            logStmt.executeUpdate();
            
            // Reload data and statistics
            loadInventoryData();
            
            JOptionPane.showMessageDialog(this, 
                "Product added to inventory successfully!\n" +
                "Initial stock: " + initialStock, 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding product to inventory: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewInventoryLog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);
            showInventoryLog(productId, productName);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to view its inventory log.", 
                "Selection Required", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showInventoryLog(int productId, String productName) {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT il.ChangeType, il.QuantityChange, il.PreviousQty, il.NewQty, il.ReferenceType, il.CreatedAt " +
                        "FROM InventoryLog il " +
                        "WHERE il.ProductID = ? " +
                        "ORDER BY il.CreatedAt DESC " +
                        "LIMIT 50";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel logModel = new DefaultTableModel(
                new String[]{"Date", "Type", "Change", "From", "To", "Reference"}, 0
            );
            
            while (rs.next()) {
                int change = rs.getInt("QuantityChange");
                String changeText = (change > 0 ? "+" : "") + change;
                
                logModel.addRow(new Object[]{
                    rs.getTimestamp("CreatedAt"),
                    rs.getString("ChangeType"),
                    changeText,
                    rs.getInt("PreviousQty"),
                    rs.getInt("NewQty"),
                    rs.getString("ReferenceType")
                });
            }
            
            JTable logTable = new JTable(logModel);
            logTable.setRowHeight(25);
            
            JScrollPane scrollPane = new JScrollPane(logTable);
            scrollPane.setPreferredSize(new Dimension(700, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Inventory Log - " + productName, 
                JOptionPane.PLAIN_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading inventory log: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}