
package com.focusflow.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/focusflow_db";

    private static final String USER = "root";

    // Replace with your MySQL password.
    // If root has no password, use "".
    private static final String PASSWORD = "windows";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver not found.", e);
        }

        return DriverManager.getConnection(
                URL, USER, PASSWORD);
    }
}
