package com.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.util.DB;

public class CategoriesPanel extends JPanel implements ActionListener {
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, addBtn, editBtn, deleteBtn;

    public CategoriesPanel() {
        initializePanel();
        loadCategoryData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"CategoryID", "Name", "CreatedAt"};
        tableModel = new DefaultTableModel(columns, 0);
        categoryTable = new JTable(tableModel);
        add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        refreshBtn = new JButton("Refresh");
        addBtn = new JButton("Add Category");
        editBtn = new JButton("Edit Category");
        deleteBtn = new JButton("Delete Category");

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

    private void loadCategoryData() {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT CategoryID, Name, CreatedAt FROM Category";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("CategoryID"),
                    rs.getString("Name"),
                    rs.getTimestamp("CreatedAt")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            loadCategoryData();
        } else if (e.getSource() == addBtn) {
            addCategory();
        } else if (e.getSource() == editBtn) {
            editCategory();
        } else if (e.getSource() == deleteBtn) {
            deleteCategory();
        }
    }

    private void addCategory() {
        JTextField nameField = new JTextField();
        
        Object[] message = {
            "Category Name:", nameField
        };
        
        int result = JOptionPane.showConfirmDialog(this, message, 
                "Add New Category", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            try (Connection conn = DB.getConnection()) {
                String sql = "INSERT INTO Category (Name) VALUES (?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameField.getText().trim());
                stmt.executeUpdate();
                loadCategoryData();
                JOptionPane.showMessageDialog(this, "Category added successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage());
            }
        }
    }

    private void editCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
            String currentName = (String) tableModel.getValueAt(selectedRow, 1);
            
            JTextField nameField = new JTextField(currentName);
            
            Object[] message = {
                "Category Name:", nameField
            };
            
            int result = JOptionPane.showConfirmDialog(this, message, 
                    "Edit Category", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
                updateCategory(categoryId, nameField.getText().trim());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a category to edit.");
        }
    }

    private void updateCategory(int categoryId, String newName) {
        try (Connection conn = DB.getConnection()) {
            String sql = "UPDATE Category SET Name = ? WHERE CategoryID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
            loadCategoryData();
            JOptionPane.showMessageDialog(this, "Category updated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating category: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
            String categoryName = (String) tableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete category: " + categoryName + "?\n" +
                "This will affect products in this category.", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DB.getConnection()) {
                    String sql = "DELETE FROM Category WHERE CategoryID = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, categoryId);
                    stmt.executeUpdate();
                    loadCategoryData();
                    JOptionPane.showMessageDialog(this, "Category deleted successfully!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.");
        }
    }
}