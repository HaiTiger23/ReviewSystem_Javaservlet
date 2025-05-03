package com.example.api.controller;

import com.example.api.util.DatabaseUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet u0111u1ec3 kiu1ec3m tra ku1ebft nu1ed1i cu01a1 su1edf du1eef liu1ec7u
 */
public class TestDatabaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiu1ebft lu1eadp response type lu00e0 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiu1ec3m tra ku1ebft nu1ed1i u0111u1ebfn cu01a1 su1edf du1eef liu1ec7u
            try (Connection conn = DatabaseUtil.getConnection()) {
                result.put("connection", "success");
                
                // Kiu1ec3m tra bu1ea3ng products
                List<Map<String, Object>> tables = new ArrayList<>();
                
                try (PreparedStatement stmt = conn.prepareStatement("SHOW TABLES")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> table = new HashMap<>();
                            table.put("name", rs.getString(1));
                            tables.add(table);
                        }
                    }
                }
                
                result.put("tables", tables);
                
                // Kiu1ec3m tra cu1ea5u tru00fac bu1ea3ng products
                List<Map<String, Object>> columns = new ArrayList<>();
                
                try (PreparedStatement stmt = conn.prepareStatement("DESCRIBE products")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> column = new HashMap<>();
                            column.put("field", rs.getString("Field"));
                            column.put("type", rs.getString("Type"));
                            column.put("null", rs.getString("Null"));
                            column.put("key", rs.getString("Key"));
                            column.put("default", rs.getString("Default"));
                            column.put("extra", rs.getString("Extra"));
                            columns.add(column);
                        }
                    }
                }
                
                result.put("products_columns", columns);
                
                // Thu1eed thu00eam mu1ed9t su1ea3n phu1ea9m mu1edbi
                try {
                    String sql = "INSERT INTO products (name, slug, description, price, category_id, user_id) VALUES (?, ?, ?, ?, ?, ?)"; 
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, "Test Product");
                        stmt.setString(2, "test-product");
                        stmt.setString(3, "This is a test product");
                        stmt.setBigDecimal(4, new java.math.BigDecimal("100000"));
                        stmt.setInt(5, 1);
                        stmt.setInt(6, 1);
                        
                        int rowsAffected = stmt.executeUpdate();
                        result.put("insert_test", rowsAffected > 0 ? "success" : "failed");
                        result.put("rows_affected", rowsAffected);
                    }
                } catch (SQLException e) {
                    result.put("insert_error", e.getMessage());
                    result.put("insert_error_code", e.getErrorCode());
                    result.put("insert_sql_state", e.getSQLState());
                }
            }
        } catch (SQLException e) {
            result.put("connection", "failed");
            result.put("error", e.getMessage());
            result.put("error_code", e.getErrorCode());
            result.put("sql_state", e.getSQLState());
            e.printStackTrace();
        }
        
        // Tru1ea3 vu1ec1 ku1ebft quu1ea3 du01b0u1edbi du1ea1ng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
}
