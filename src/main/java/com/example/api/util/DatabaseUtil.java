package com.example.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp tiện ích để quản lý kết nối đến cơ sở dữ liệu MySQL
 */
public class DatabaseUtil {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/review_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "ServBay.dev";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy kết nối đến cơ sở dữ liệu
     * 
     * @return Connection đến cơ sở dữ liệu
     * @throws SQLException nếu không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Đóng kết nối an toàn
     * 
     * @param connection Kết nối cần đóng
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
