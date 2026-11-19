package com.oopj.orders;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String USER = "root";

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("MYSQL_PASSWORD");
        if (password == null) {
            throw new SQLException("Set the MYSQL_PASSWORD environment variable before connecting.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }
}