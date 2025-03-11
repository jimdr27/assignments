/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;



public class Shopping {
    
    

    private JButton goToCartButton;
    private JPanel adminPanel;
    private JButton ADDITEMButton;
    private JTable table1;
    private JLabel imageLabel;
    private JButton CLOSEButton;
    private JLabel itemName;
    private JLabel price;
    private JTextField quantity;

    JFrame shopF = new JFrame();

    // Constructor
    public Shopping() {
        initComponents();  // Call to initialize components

        if (adminPanel == null) {
            System.out.println("adminPanel is null");
        }

        shopF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        shopF.setContentPane(adminPanel);
        shopF.pack();
        shopF.setLocationRelativeTo(null);
        tableData();
        shopF.setVisible(true);

        // Add event listeners
        goToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shopF.dispose();
                new Cart();
            }
        });

        ADDITEMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (quantity.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Please Fill All Fields to add Item");
                } else {
                    String sql = "insert into cart (ITEM_NAME, PRICE, QUANTITY, TOTAL) values (?,?,?,?)";

                    try {
                        int total = Integer.parseInt(price.getText()) * Integer.parseInt(quantity.getText());

                        Class.forName("com.mysql.cj.jdbc.Driver");
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/intern", "root", "jimbench123!");
                        PreparedStatement statement = connection.prepareStatement(sql);

                        statement.setString(1, itemName.getText());
                        statement.setString(2, price.getText());
                        statement.setString(3, quantity.getText());
                        statement.setInt(4, total);

                        statement.executeUpdate();

                        JOptionPane.showMessageDialog(null, "ITEM ADDED SUCCESSFULLY");
                        itemName.setText("");
                        price.setText("");
                        imageLabel.setIcon(null);
                        quantity.setText("");

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
            }
        });

        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultTableModel dm = (DefaultTableModel) table1.getModel();
                int selectedRow = table1.getSelectedRow();
                itemName.setText(dm.getValueAt(selectedRow, 0).toString());

                byte[] img = (byte[]) dm.getValueAt(selectedRow, 2);
                ImageIcon imageIcon = new ImageIcon(img);
                Image im = imageIcon.getImage();
                Image newimg = im.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                ImageIcon finalPic = new ImageIcon(newimg);

                imageLabel.setIcon(finalPic);
                price.setText(dm.getValueAt(selectedRow, 1).toString());
            }
        });

        CLOSEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shopF.dispose();
            }
        });
    }

    // Initialization of components
    private void initComponents() {
    // Initialize adminPanel and other components
    adminPanel = new JPanel();
    goToCartButton = new JButton("Go To Cart");
    ADDITEMButton = new JButton("Add Item");
    table1 = new JTable();
    imageLabel = new JLabel();
    CLOSEButton = new JButton("Close");
    itemName = new JLabel("Item Name");
    price = new JLabel("Price");
    quantity = new JTextField();

    // Set layout for the adminPanel
    adminPanel.setLayout(new BorderLayout());

    JPanel topPanel = new JPanel(new GridLayout(1, 2));
    JPanel bottomPanel = new JPanel(new FlowLayout());

    // Setup top panel
    JPanel itemDetailsPanel = new JPanel(new GridLayout(3, 2));
    itemDetailsPanel.add(new JLabel("Item Name:"));
    itemDetailsPanel.add(itemName);
    itemDetailsPanel.add(new JLabel("Price:"));
    itemDetailsPanel.add(price);
    itemDetailsPanel.add(new JLabel("Quantity:"));
    itemDetailsPanel.add(quantity);

    topPanel.add(itemDetailsPanel);
    topPanel.add(imageLabel);

    // Setup bottom panel
    bottomPanel.add(goToCartButton);
    bottomPanel.add(ADDITEMButton);
    bottomPanel.add(CLOSEButton);

    adminPanel.add(topPanel, BorderLayout.NORTH);
    adminPanel.add(new JScrollPane(table1), BorderLayout.CENTER);
    adminPanel.add(bottomPanel, BorderLayout.SOUTH);
}



    public void tableData() {
        try {
            String a = "Select * from shopping";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/intern", "root", "jimbench123!");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(a);
            table1.setModel(buildTableModel(rs));

        } catch (Exception ex1) {
            JOptionPane.showMessageDialog(null, ex1.getMessage());
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
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
