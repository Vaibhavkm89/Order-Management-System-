package com.oopj.orders;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        createTable();
        while (true) {
            System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
            System.out.println("1. Insert three sample orders");
            System.out.println("2. Search by status (quantity > 2)");
            System.out.println("3. Increase order quantity");
            System.out.println("4. Update order status");
            System.out.println("5. Display total order value");
            System.out.println("6. Delete order (only if Cancelled)");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> insertSampleOrders();
                case 2 -> searchOrders();
                case 3 -> increaseQuantity();
                case 4 -> updateStatus();
                case 5 -> showTotalValue();
                case 6 -> deleteCancelledOrder();
                case 0 -> { System.out.println("Thank you!"); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Orders (" +
                "order_id INT PRIMARY KEY, customer_name VARCHAR(100), " +
                "product_name VARCHAR(100), quantity INT, unit_price DOUBLE, status VARCHAR(30))";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("Connected to ecommerce_db. Orders table is ready.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Check MySQL is running and DBConnection password is correct.");
            System.exit(1);
        }
    }

    private static void insertSampleOrders() {
        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] orders = {
            {101, "Aarav", "Keyboard", 3, 799.0, "Pending"},
            {102, "Diya", "Mouse", 2, 499.0, "Shipped"},
            {103, "Kabir", "Monitor", 4, 8999.0, "Cancelled"}
        };
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] o : orders) {
                ps.setInt(1, (int)o[0]);
                ps.setString(2, (String)o[1]);
                ps.setString(3, (String)o[2]);
                ps.setInt(4, (int)o[3]);
                ps.setDouble(5, (double)o[4]);
                ps.setString(6, (String)o[5]);
                try {
                    ps.executeUpdate();
                    System.out.println("Inserted order ID: " + o[0]);
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Order ID " + o[0] + " already exists. Skipped.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
        }
    }

    private static void searchOrders() {
        System.out.print("Enter status (Pending/Shipped/Delivered/Cancelled): ");
        String status = sc.nextLine();
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) { found = true; printOrder(rs); }
                if (!found) System.out.println("No matching orders found.");
            }
        } catch (SQLException e) { System.out.println("Search error: " + e.getMessage()); }
    }

    private static void increaseQuantity() {
        System.out.print("Enter order ID: ");
        int id = readInt();
        System.out.print("Enter additional quantity: ");
        int extra = readInt();
        if (extra <= 0) { System.out.println("Quantity must be greater than zero."); return; }
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, extra);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Quantity updated." : "Order does not exist.");
        } catch (SQLException e) { System.out.println("Update error: " + e.getMessage()); }
    }

    private static void updateStatus() {
        System.out.print("Enter order ID: ");
        int id = readInt();
        String findSql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement find = con.prepareStatement(findSql)) {
            find.setInt(1, id);
            try (ResultSet rs = find.executeQuery()) {
                if (!rs.next()) { System.out.println("Order does not exist."); return; }
                System.out.println("Current order:");
                printOrder(rs);
            }

            System.out.print("Enter new status (Pending/Shipped/Delivered): ");
            String status = sc.nextLine();
            if (!status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Shipped")
                    && !status.equalsIgnoreCase("Delivered")) {
                System.out.println("Invalid status. No changes made.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Orders SET status = ? WHERE order_id = ?")) {
                ps.setString(1, capitalize(status));
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            System.out.println("Updated order:");
            try (PreparedStatement ps = con.prepareStatement(findSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) printOrder(rs); }
            }
        } catch (SQLException e) { System.out.println("Status update error: " + e.getMessage()); }
    }

    private static void showTotalValue() {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) AS total_value FROM Orders";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) System.out.printf("Total order value: %.2f%n", rs.getDouble("total_value"));
        } catch (SQLException e) { System.out.println("Calculation error: " + e.getMessage()); }
    }

    private static void deleteCancelledOrder() {
        System.out.print("Enter order ID to delete: ");
        int id = readInt();
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Cancelled order deleted."
                    : "Order not found or status is not Cancelled. Cannot delete.");
        } catch (SQLException e) { System.out.println("Delete error: " + e.getMessage()); }
    }

    private static void printOrder(ResultSet rs) throws SQLException {
        System.out.printf("ID: %d | Customer: %s | Product: %s | Quantity: %d | Price: %.2f | Status: %s%n",
                rs.getInt("order_id"), rs.getString("customer_name"), rs.getString("product_name"),
                rs.getInt("quantity"), rs.getDouble("unit_price"), rs.getString("status"));
    }

    private static String capitalize(String s) {
        s = s.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static int readInt() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine()); }
            catch (NumberFormatException e) { System.out.print("Enter a valid whole number: "); }
        }
    }
}