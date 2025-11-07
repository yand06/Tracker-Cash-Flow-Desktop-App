package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/personal_expense_tracker";
        String username = "root";
        String password = "Jakarta2025";

        return DriverManager.getConnection(url, username, password);
    }
}
