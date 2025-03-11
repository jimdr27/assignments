/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package helloworld;

/**
 *
 * @author jimdr
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

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

class EShop {
    private Connection conn;
    private User loggedInUser;

    public EShop() {
    try {
        // Connect to MySQL database
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eshop_db", "root", "jimbench123!");
        // Create tables if they don't exist
        createTables();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    private void createTables() throws SQLException {
        // Δημιουργία πίνακα χρηστών
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY," +
                "password VARCHAR(100) NOT NULL," +
                "is_admin BOOLEAN NOT NULL)";
        // Δημιουργία πίνακα προϊόντων
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
            pstmt.setBoolean(3, false);  // Όλοι οι νέοι χρήστες είναι μη admin
            pstmt.executeUpdate();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            System.out.println("Username already exists. Please choose another.");
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
                System.out.println("Login successful. Welcome, " + username + "!");
                return true;
            } else {
                System.out.println("Invalid username or password.");
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayProducts() {
    String sql = "SELECT * FROM products";
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        System.out.println("Available Products:");
        int i = 1;
        while (rs.next()) {
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");

            String stockMessage = (stock == 0) ? "This product is currently out of stock" : "Stock: " + stock;
            System.out.println(i++ + ". " + name + " - $" + price + " - " + stockMessage);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public void purchaseProduct(int productIndex, int quantity) {
        if (loggedInUser == null) {
            System.out.println("Please log in to make a purchase.");
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
                    System.out.println("Sorry, not enough stock available.");
                } else {
                    // Ενημέρωση του αποθέματος
                    String updateStockSql = "UPDATE products SET stock = stock - ? WHERE name = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateStockSql)) {
                        updatePstmt.setInt(1, quantity);
                        updatePstmt.setString(2, name);
                        updatePstmt.executeUpdate();
                        System.out.println("You have successfully purchased " + quantity + " " + name + "(s).");
                        System.out.println("Total cost: $" + price * quantity);
                    }
                }
            } else {
                System.out.println("Product not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void manageProducts() {
        if (loggedInUser != null && loggedInUser.isAdmin()) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Admin Panel - Manage Products");
                System.out.println("1. Add Stock");
                System.out.println("2. Change Price");
                System.out.println("3. Exit Admin Panel");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();

                if (choice == 1) {
                    System.out.print("Enter the product number to add stock: ");
                    int productIndex = scanner.nextInt() - 1;
                    System.out.print("Enter the quantity to add: ");
                    int quantity = scanner.nextInt();
                    updateProductStock(productIndex, quantity);
                    System.out.println("Stock updated successfully.");
                } else if (choice == 2) {
                    System.out.print("Enter the product number to change price: ");
                    int productIndex = scanner.nextInt() - 1;
                    System.out.print("Enter the new price: ");
                    double price = scanner.nextDouble();
                    updateProductPrice(productIndex, price);
                    System.out.println("Price updated successfully.");
                } else if (choice == 3) {
                    System.out.println("Exiting Admin Panel.");
                    break;
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }
        } 
    }

    private void updateProductStock(int productIndex, int quantity) {
        String sql = "UPDATE products SET stock = stock + ? WHERE name = (SELECT name FROM products LIMIT 1 OFFSET ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productIndex);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProductPrice(int productIndex, double price) {
        String sql = "UPDATE products SET price = ? WHERE name = (SELECT name FROM products LIMIT 1 OFFSET ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, price);
            pstmt.setInt(2, productIndex);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class HelloWorld {
    public static void main(String[] args) {
        // Create an e-shop
        EShop shop = new EShop();

        // Προσθήκη προϊόντων στη βάση δεδομένων (αν δεν υπάρχουν ήδη)
        shop.addProduct("Laptop", 999.99, 10);
        shop.addProduct("Smartphone", 599.99, 20);
        shop.addProduct("Tablet", 399.99, 15);
        shop.addProduct("Headphones", 199.99, 50);
        shop.addProduct("Smartwatch", 249.99, 30);
        shop.addProduct("Camera", 499.99, 12);

        // Create a scanner for user input
        Scanner scanner = new Scanner(System.in);

        // User registration and login
        while (true) {
            System.out.println("Welcome to the E-Shop!");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                shop.registerUser(username, password);
            } else if (choice == 2) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                if (shop.loginUser(username, password)) {
                    break;
                }
            } else if (choice == 3) {
                System.out.println("Thank you for visiting the E-Shop!");
                scanner.close();
                return;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }

        
            
           while (true) {
            
                shop.manageProducts();
            

            // Display product details
            shop.displayProducts();

            // Purchase process
            System.out.println("1. Purchase a product");
            System.out.println("2. Logout");
            
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();

            if (option == 1) {
                System.out.print("Enter the product number you want to purchase: ");
                int productIndex = scanner.nextInt() - 1;

                System.out.print("Enter the quantity you want to purchase: ");
                int quantity = scanner.nextInt();

                shop.purchaseProduct(productIndex, quantity);

                // Display remaining stock
                shop.displayProducts();
            } else if (option == 2) {
                // Logging out will break the loop and return to the main menu
                System.out.println("Logging out...");
                break;
            }  else {
                System.out.println("Invalid option. Please try again.");
            }
        }

        // Close scanner (optional as scanner.close() is called before return in both cases)
        scanner.close();
    }
}
