package com.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class PaymentPanel extends JPanel implements ActionListener {
    private String userRole;
    private int userId;
    private String userName;
    
    private JComboBox<String> pendingOrdersCombo;
    private JTextArea orderDetailsArea;
    private JComboBox<String> paymentMethodCombo;
    private JButton processPaymentBtn, refreshBtn;
    
    public PaymentPanel(String userRole, int userId, String userName) {
        this.userRole = userRole;
        this.userId = userId;
        this.userName = userName;
        initializePanel();
        loadPendingOrders();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Payment Processing", JLabel.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("User: " + userName + " (" + userRole + ")", JLabel.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.LIGHT_GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Order Selection Panel
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Select Order to Pay"));
        
        JPanel orderSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        orderSelectionPanel.add(new JLabel("Pending Orders:"));
        pendingOrdersCombo = new JComboBox<>();
        pendingOrdersCombo.addActionListener(this);
        orderSelectionPanel.add(pendingOrdersCombo);
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(this);
        orderSelectionPanel.add(refreshBtn);
        
        orderPanel.add(orderSelectionPanel, BorderLayout.NORTH);
        
        orderDetailsArea = new JTextArea(8, 50);
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        orderPanel.add(new JScrollPane(orderDetailsArea), BorderLayout.CENTER);
        
        // Payment Panel
        JPanel paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        
        JPanel paymentFormPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        paymentFormPanel.add(new JLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"CreditCard", "PayPal", "BankTransfer", "Cash"});
        paymentFormPanel.add(paymentMethodCombo);
        
        paymentFormPanel.add(new JLabel("")); // Empty cell
        paymentFormPanel.add(new JLabel("")); // Empty cell
        
        processPaymentBtn = new JButton("💳 Process Payment");
        processPaymentBtn.addActionListener(this);
        processPaymentBtn.setBackground(new Color(46, 204, 113));
        processPaymentBtn.setForeground(Color.WHITE);
        processPaymentBtn.setFont(new Font("Arial", Font.BOLD, 14));
        paymentFormPanel.add(processPaymentBtn);
        
        paymentPanel.add(paymentFormPanel, BorderLayout.CENTER);
        
        mainPanel.add(orderPanel);
        mainPanel.add(paymentPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadPendingOrders() {
        pendingOrdersCombo.removeAllItems();
        
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT o.OrderID, o.OrderNumber, o.TotalAmount, o.CreatedAt " +
                        "FROM `order` o " +
                        "WHERE o.UserID = ? AND o.Status = 'Pending' " +
                        "ORDER BY o.CreatedAt DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String display = String.format("Order #%s - $%.2f - %s", 
                    rs.getString("OrderNumber"),
                    rs.getDouble("TotalAmount"),
                    rs.getTimestamp("CreatedAt"));
                pendingOrdersCombo.addItem(display);
            }
            
            if (pendingOrdersCombo.getItemCount() == 0) {
                pendingOrdersCombo.addItem("No pending orders found");
                processPaymentBtn.setEnabled(false);
            } else {
                processPaymentBtn.setEnabled(true);
                loadOrderDetails();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    private void loadOrderDetails() {
        if (pendingOrdersCombo.getSelectedIndex() <= 0) return;
        
        String selected = (String) pendingOrdersCombo.getSelectedItem();
        if (selected.startsWith("No pending")) return;
        
        String orderNumber = selected.split(" - ")[0].replace("Order #", "");
        
        try (Connection conn = DB.getConnection()) {
            // Get order details
            String orderSql = "SELECT o.*, u.Username, u.FullName " +
                            "FROM `order` o " +
                            "JOIN user u ON o.UserID = u.UserID " +
                            "WHERE o.OrderNumber = ?";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setString(1, orderNumber);
            ResultSet orderRs = orderStmt.executeQuery();
            
            StringBuilder details = new StringBuilder();
            if (orderRs.next()) {
                details.append("Order Number: ").append(orderRs.getString("OrderNumber")).append("\n");
                details.append("Customer: ").append(orderRs.getString("FullName")).append(" (").append(orderRs.getString("Username")).append(")\n");
                details.append("Total Amount: $").append(String.format("%.2f", orderRs.getDouble("TotalAmount"))).append("\n");
                details.append("Order Date: ").append(orderRs.getTimestamp("CreatedAt")).append("\n");
                details.append("Status: ").append(orderRs.getString("Status")).append("\n\n");
                
                // Get order items
                String itemsSql = "SELECT oi.*, p.Name as ProductName, p.PriceOrValue " +
                                "FROM orderitem oi " +
                                "JOIN product p ON oi.ProductID = p.ProductID " +
                                "WHERE oi.OrderID = ?";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsSql);
                itemsStmt.setInt(1, orderRs.getInt("OrderID"));
                ResultSet itemsRs = itemsStmt.executeQuery();
                
                details.append("Order Items:\n");
                details.append("----------------------------------------\n");
                while (itemsRs.next()) {
                    details.append(String.format("%s x%d @ $%.2f = $%.2f\n",
                        itemsRs.getString("ProductName"),
                        itemsRs.getInt("Quantity"),
                        itemsRs.getDouble("UnitPrice"),
                        itemsRs.getDouble("TotalPrice")));
                }
            }
            
            orderDetailsArea.setText(details.toString());
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading order details: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    private void processPayment() {
        if (pendingOrdersCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to pay", 
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selected = (String) pendingOrdersCombo.getSelectedItem();
        String orderNumber = selected.split(" - ")[0].replace("Order #", "");
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Process payment for " + selected + "?\nPayment Method: " + paymentMethod,
            "Confirm Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            processPaymentInDatabase(orderNumber, paymentMethod);
        }
    }
    
    private void processPaymentInDatabase(String orderNumber, String paymentMethod) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Get order details and verify stock availability
            String orderSql = "SELECT o.OrderID, o.TotalAmount, oi.ProductID, oi.Quantity, i.Available " +
                            "FROM `order` o " +
                            "JOIN orderitem oi ON o.OrderID = oi.OrderID " +
                            "JOIN inventory i ON oi.ProductID = i.ProductID " +
                            "WHERE o.OrderNumber = ? AND o.Status = 'Pending'";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setString(1, orderNumber);
            ResultSet rs = orderStmt.executeQuery();
            
            int orderId = -1;
            double totalAmount = 0;
            boolean stockAvailable = true;
            StringBuilder stockIssues = new StringBuilder();
            
            while (rs.next()) {
                if (orderId == -1) {
                    orderId = rs.getInt("OrderID");
                    totalAmount = rs.getDouble("TotalAmount");
                }
                
                int productId = rs.getInt("ProductID");
                int quantity = rs.getInt("Quantity");
                int availableStock = rs.getInt("Available");
                
                if (availableStock < quantity) {
                    stockAvailable = false;
                    stockIssues.append(String.format("Product ID %d: Requested %d, Available %d\n", 
                        productId, quantity, availableStock));
                }
            }
            
            if (!stockAvailable) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient stock for this order:\n" + stockIssues.toString() + 
                    "\nPlease update the order or contact support.", 
                    "Stock Unavailable", JOptionPane.ERROR_MESSAGE);
                conn.rollback();
                return;
            }
            
            // 2. Create payment record
            String paymentSql = "INSERT INTO payment (Amount, Type, Status, Date) VALUES (?, ?, 'Completed', NOW())";
            PreparedStatement paymentStmt = conn.prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS);
            paymentStmt.setDouble(1, totalAmount);
            paymentStmt.setString(2, paymentMethod);
            paymentStmt.executeUpdate();
            
            ResultSet paymentRs = paymentStmt.getGeneratedKeys();
            int paymentId = -1;
            if (paymentRs.next()) {
                paymentId = paymentRs.getInt(1);
            }
            
            // 3. Link payment to order
            String orderPaymentSql = "INSERT INTO orderpayment (OrderID, PaymentID, Amount) VALUES (?, ?, ?)";
            PreparedStatement orderPaymentStmt = conn.prepareStatement(orderPaymentSql);
            orderPaymentStmt.setInt(1, orderId);
            orderPaymentStmt.setInt(2, paymentId);
            orderPaymentStmt.setDouble(3, totalAmount);
            orderPaymentStmt.executeUpdate();
            
            // 4. Update order status to Confirmed
            String updateOrderSql = "UPDATE `order` SET Status = 'Confirmed' WHERE OrderID = ?";
            PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql);
            updateOrderStmt.setInt(1, orderId);
            updateOrderStmt.executeUpdate();
            
            // 5. DECREMENT STOCK - This happens only after successful payment
            String inventorySql = "UPDATE inventory i " +
                                "JOIN orderitem oi ON i.ProductID = oi.ProductID " +
                                "SET i.Available = i.Available - oi.Quantity " +
                                "WHERE oi.OrderID = ?";
            PreparedStatement inventoryStmt = conn.prepareStatement(inventorySql);
            inventoryStmt.setInt(1, orderId);
            int updatedRows = inventoryStmt.executeUpdate();
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, 
                "Payment processed successfully!\n" +
                "Order Number: " + orderNumber + "\n" +
                "Amount: $" + String.format("%.2f", totalAmount) + "\n" +
                "Payment Method: " + paymentMethod + "\n" +
                "Stock has been reserved for your order.",
                "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the interface
            loadPendingOrders();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage(), 
                "Payment Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn || e.getSource() == pendingOrdersCombo) {
            loadPendingOrders();
        } else if (e.getSource() == processPaymentBtn) {
            processPayment();
        }
    }
}