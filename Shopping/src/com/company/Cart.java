/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class Cart {

    private JTable table1;
    private JButton buyNowButton;
    private JPanel cartPanel;
    private JLabel total;
    private JButton BACKButton;
    private JFrame cartF;

    // Constructor
    public Cart() {
        // Initialize the components
        initComponents();

        // Setup JFrame properties
        cartF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cartF.setContentPane(cartPanel);
        cartF.pack();
        cartF.setLocationRelativeTo(null);
        cartF.setVisible(true);

        // Set the total label to display the total amount
        total.setText("Total: $" + count());

        // Populate the table with data from the cart table
        tableData();

        // Event listener for "Buy Now" button
        buyNowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBuyNowAction();
            }
        });

        // Event listener for "Back" button
        BACKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cartF.dispose(); // Close the cart window
                new Shopping();  // Open the shopping window
            }
        });
    }

    // Method to initialize components
    private void initComponents() {
        cartF = new JFrame("Your Cart");
        cartPanel = new JPanel(new BorderLayout());
        table1 = new JTable();
        buyNowButton = new JButton("Buy Now");
        BACKButton = new JButton("Back");
        total = new JLabel("Total: $0");

        // Bottom panel for total and buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(total);
        bottomPanel.add(buyNowButton);
        bottomPanel.add(BACKButton);

        // Adding components to cartPanel
        cartPanel.add(new JScrollPane(table1), BorderLayout.CENTER);
        cartPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // Method to handle the "Buy Now" button click
    private void handleBuyNowAction() {
        try {
            // Show a success message with the total amount
            JOptionPane.showMessageDialog(null, "Items purchased successfully. Amount payable: $" + count());

            // SQL to clear the cart
            String sql = "DELETE FROM cart";

            // Execute the SQL command
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/intern", "root", "jimbench123!");
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
            }

            // Refresh the table data after clearing the cart
            tableData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Method to retrieve and display data from the cart table
    public void tableData() {
        try {
            // SQL to select all items from the cart table
            String sql = "SELECT * FROM cart";

            // Execute the query and populate the table
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/intern", "root", "jimbench123!");
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                table1.setModel(buildTableModel(rs));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Method to calculate the total amount
    public int count() {
        int totalAmount = 0;

        try {
            // SQL to select the total from each item in the cart table
            String sql = "SELECT TOTAL FROM cart";

            // Execute the query and sum up the totals
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/intern", "root", "jimbench123!");
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                while (rs.next()) {
                    totalAmount += rs.getInt("TOTAL");
                }
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        return totalAmount;
    }

    // Method to build a table model from a ResultSet
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get column names
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Get rows of data
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
}
