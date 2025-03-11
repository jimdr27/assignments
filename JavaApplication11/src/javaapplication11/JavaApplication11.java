/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaapplication11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

// Product class
class Product {
    private String name;
    private double price;
    private int stock;

    public Product(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void reduceStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
        } else {
            System.out.println("Not enough stock available.");
        }
    }

    public void addStock(int quantity) {
        stock += quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

// User class
class User {
    private String username;
    private String password;
    private boolean isAdmin;

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

// EShop class
class EShop {
    private Connection conn;
    public User loggedInUser;

    public EShop() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eshop_db", "root", "jimbench123!");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY," +
                "password VARCHAR(100) NOT NULL," +
                "is_admin BOOLEAN NOT NULL)";
        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "name VARCHAR(100) PRIMARY KEY," +
                "price DECIMAL(10, 2) NOT NULL," +
                "stock INT NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createProductsTable);
        }
    }

    public void registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password, is_admin) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "User registered successfully.", "Registration", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Username already exists. Please choose another.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInUser = new User(rs.getString("username"), rs.getString("password"), rs.getBoolean("is_admin"));
                JOptionPane.showMessageDialog(null, "Login successful. Welcome, " + username + "!", "Login", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addProduct(String name, double price, int stock) {
        String sql = "INSERT INTO products(name, price, stock) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Product added successfully.", "Product Management", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeProduct(int productIndex) {
        if (!loggedInUser.isAdmin()) {
            JOptionPane.showMessageDialog(null, "Only admins can remove products.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM products LIMIT 1 OFFSET ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String deleteProductSql = "DELETE FROM products WHERE name = ?";
                try (PreparedStatement deletePstmt = conn.prepareStatement(deleteProductSql)) {
                    deletePstmt.setString(1, name);
                    deletePstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Product " + name + " has been removed successfully.", "Product Management", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Product not found.", "Product Management", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String displayProducts() {
        StringBuilder productList = new StringBuilder("Available Products:\n");
        String sql = "SELECT * FROM products";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int i = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                String stockMessage = (stock == 0) ? "This product is currently out of stock" : "Stock: " + stock;
                productList.append(i++).append(". ").append(name).append(" - $").append(price).append(" - ").append(stockMessage).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList.toString();
    }

    public void purchaseProduct(int productIndex, int quantity) {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(null, "Please log in to make a purchase.", "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM products LIMIT 1 OFFSET ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");

                if (quantity > stock) {
                    JOptionPane.showMessageDialog(null, "Sorry, not enough stock available.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    String updateStockSql = "UPDATE products SET stock = stock - ? WHERE name = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateStockSql)) {
                        updatePstmt.setInt(1, quantity);
                        updatePstmt.setString(2, name);
                        updatePstmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, "You have successfully purchased " + quantity + " " + name + "(s).\nTotal cost: $" + (price * quantity), "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Product not found.", "Product Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void increaseProductStock(int productIndex, int quantity) {
        if (!loggedInUser.isAdmin()) {
            JOptionPane.showMessageDialog(null, "Only admins can increase stock.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM products LIMIT 1 OFFSET ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                int currentStock = rs.getInt("stock");

                String updateStockSql = "UPDATE products SET stock = ? WHERE name = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateStockSql)) {
                    updatePstmt.setInt(1, currentStock + quantity);
                    updatePstmt.setString(2, name);
                    updatePstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Stock for " + name + " increased by " + quantity, "Product Management", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Product not found.", "Product Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeProductPrice(int productIndex, double newPrice) {
        if (!loggedInUser.isAdmin()) {
            JOptionPane.showMessageDialog(null, "Only admins can change prices.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM products LIMIT 1 OFFSET ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String updatePriceSql = "UPDATE products SET price = ? WHERE name = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updatePriceSql)) {
                    updatePstmt.setDouble(1, newPrice);
                    updatePstmt.setString(2, name);
                    updatePstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Price for " + name + " updated to $" + newPrice, "Product Management", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Product not found.", "Product Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Login/Register Frame
class LoginRegisterFrame extends JFrame {
    private EShop shop;

    public LoginRegisterFrame(EShop shop) {
        this.shop = shop;

        setTitle("Login/Register");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Login action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (shop.loginUser(username, password)) {
                    dispose();
                    new ShopFrame(shop);
                }
            }
        });

        // Register action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                shop.registerUser(username, password);
            }
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }
}

// Shop Frame
// Shop Frame
class ShopFrame extends JFrame {
    private EShop shop;
    private JTextArea productArea; // Make productArea a class member

    public ShopFrame(EShop shop) {
        this.shop = shop;

        setTitle("E-Shop");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        productArea = new JTextArea(); // Initialize here
        productArea.setEditable(false);
        productArea.setText(shop.displayProducts());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton purchaseButton = new JButton("Purchase");
        JButton refreshButton = new JButton("Refresh"); // New Refresh button
        JButton logoutButton = new JButton("Logout");
        
        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                productArea.setText(shop.displayProducts()); // Refresh the product display
            }
        });

        // Purchase button action
        purchaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Enter the product number and quantity (e.g., 1 2):");
                if (input != null) {
                    String[] parts = input.split(" ");
                    if (parts.length == 2) {
                        try {
                            int productIndex = Integer.parseInt(parts[0]) - 1; // Adjust for 0-based index
                            int quantity = Integer.parseInt(parts[1]);
                            shop.purchaseProduct(productIndex, quantity);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter both product number and quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Logout button action
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shop.loggedInUser = null; // Clear the logged-in user
                dispose(); // Close the shop frame
                new LoginRegisterFrame(shop); // Return to login/register frame
            }
        });

        // Admin actions
        if (shop.loggedInUser != null && shop.loggedInUser.isAdmin()) {
            JButton addProductButton = new JButton("Add Product");
            JButton removeProductButton = new JButton("Remove Product");
            JButton changePriceButton = new JButton("Change Price");
            JButton increaseStockButton = new JButton("Increase Stock");

            // Add Product action
            addProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog("Enter product name:");
                    double price = Double.parseDouble(JOptionPane.showInputDialog("Enter product price:"));
                    int stock = Integer.parseInt(JOptionPane.showInputDialog("Enter product stock:"));
                    shop.addProduct(name, price, stock);
                    productArea.setText(shop.displayProducts()); // Refresh the product display after adding
                }
            });

            // Remove Product action
            removeProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int productIndex = Integer.parseInt(JOptionPane.showInputDialog("Enter product number to remove:")) - 1;
                    shop.removeProduct(productIndex);
                    productArea.setText(shop.displayProducts()); // Refresh after removing
                }
            });

            // Change Price action
            changePriceButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int productIndex = Integer.parseInt(JOptionPane.showInputDialog("Enter product number to change price:")) - 1;
                    double newPrice = Double.parseDouble(JOptionPane.showInputDialog("Enter new price:"));
                    shop.changeProductPrice(productIndex, newPrice);
                    productArea.setText(shop.displayProducts()); // Refresh after changing price
                }
            });

            // Increase Stock action
            increaseStockButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int productIndex = Integer.parseInt(JOptionPane.showInputDialog("Enter product number to increase stock:")) - 1;
                    int quantity = Integer.parseInt(JOptionPane.showInputDialog("Enter quantity to increase:"));
                    shop.increaseProductStock(productIndex, quantity);
                    productArea.setText(shop.displayProducts()); // Refresh after increasing stock
                }
            });

            buttonPanel.add(addProductButton);
            buttonPanel.add(removeProductButton);
            buttonPanel.add(changePriceButton);
            buttonPanel.add(increaseStockButton);
        }

        buttonPanel.add(purchaseButton);
        buttonPanel.add(refreshButton); // Add refresh button to the panel
        buttonPanel.add(logoutButton); // Add logout button

        panel.add(new JScrollPane(productArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);

        setVisible(true);
    }
}


// Main Class
public class JavaApplication11 {
    public static void main(String[] args) {
        EShop shop = new EShop();
        new LoginRegisterFrame(shop);
    }
}
