package com.example.api.servlet;

import com.example.api.controller.AdminCategoryController;
import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/categories/*")
public class AdminCategoryServlet extends HttpServlet {
    private final AdminCategoryController adminCategoryController;
    private final Gson gson;

    public AdminCategoryServlet() {
        this.adminCategoryController = new AdminCategoryController();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> result;
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Lấy danh sách danh mục
                result = adminCategoryController.getAllCategories(request, response);
            } else {
                // Lấy chi tiết danh mục
                int categoryId = Integer.parseInt(pathInfo.substring(1));
                result = adminCategoryController.getCategoryById(categoryId, request, response);
            }
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Đọc dữ liệu JSON từ request body
            JsonObject data = gson.fromJson(request.getReader(), JsonObject.class);
            Map<String, Object> result = adminCategoryController.addCategory(data, request, response);
            sendJsonResponse(response, result);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID danh mục");
            return;
        }

        try {
            // Lấy ID danh mục từ URL
            int categoryId = Integer.parseInt(pathInfo.substring(1));
            
            // Đọc dữ liệu JSON từ request body
            JsonObject data = gson.fromJson(request.getReader(), JsonObject.class);
            
            Map<String, Object> result = adminCategoryController.updateCategory(categoryId, data, request, response);
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID danh mục");
            return;
        }

        try {
            // Lấy ID danh mục từ URL
            int categoryId = Integer.parseInt(pathInfo.substring(1));
            Map<String, Object> result = adminCategoryController.deleteCategory(categoryId, request, response);
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    /**
     * Gửi response dạng JSON
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    /**
     * Gửi response lỗi
     */
    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        sendJsonResponse(response, Map.of(
            "success", false,
            "message", message
        ));
    }
} 