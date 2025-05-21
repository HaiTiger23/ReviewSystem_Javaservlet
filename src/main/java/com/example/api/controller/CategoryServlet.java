package com.example.api.controller;

import com.example.api.model.Category;
import com.example.api.util.JwtUtil;
import com.example.api.util.SlugUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet xử lý các request API cho danh mục
 */
@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CategoryController categoryController;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        categoryController = new CategoryController();
        gson = new Gson();
    }
    
    /**
     * Xuất request GET để lấy danh mục
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result;
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Lấy tất cả danh mục
            result = categoryController.getAllCategories(request, response);
        } else {
            // Lấy danh mục theo ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    result = categoryController.getCategoryById(categoryId, request, response);
                } catch (NumberFormatException e) {
                    result = new HashMap<>();
                    result.put("error", "ID danh mục không hợp lệ");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result = new HashMap<>();
                result.put("error", "URL không hợp lệ");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xuất request POST để thêm danh mục mới
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Parse JSON từ request body
        JsonObject jsonRequest = parseJsonRequest(request);
        Map<String, Object> result;
        
        if (jsonRequest != null) {
            result = categoryController.addCategory(jsonRequest, request, response);
        } else {
            result = new HashMap<>();
            result.put("error", "Dữ liệu không hợp lệ");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xuất request PUT để cập nhật danh mục
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();
        
        if (pathInfo != null && !pathInfo.equals("/")) {
            // Lấy ID danh mục từ URL
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    
                    // Parse JSON từ request body
                    JsonObject jsonRequest = parseJsonRequest(request);
                    
                    if (jsonRequest != null) {
                        result = categoryController.updateCategory(categoryId, jsonRequest, request, response);
                    } else {
                        result.put("error", "Dữ liệu không hợp lệ");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } catch (NumberFormatException e) {
                    result.put("error", "ID danh mục không hợp lệ");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result.put("error", "URL không hợp lệ");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            result.put("error", "Cần chuẩn ID danh mục");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xuất request DELETE để xóa danh mục
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();
        
        if (pathInfo != null && !pathInfo.equals("/")) {
            // Lấy ID danh mục từ URL
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    result = categoryController.deleteCategory(categoryId, request, response);
                } catch (NumberFormatException e) {
                    result.put("error", "ID danh mục không hợp lệ");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result.put("error", "URL không hợp lệ");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            result.put("error", "Cần chuẩn ID danh mục");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Parse JSON từ request body
     */
    private JsonObject parseJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        String line;
        
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        
        String data = buffer.toString();
        if (data.isEmpty()) {
            return null;
        }
        
        try {
            return JsonParser.parseString(data).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }
}
