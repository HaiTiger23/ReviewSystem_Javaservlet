package com.example.api.controller;

import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet xử lý các yêu cầu API quản lý sản phẩm dành cho Admin
 * Tất cả các endpoint đều yêu cầu quyền admin và bắt đầu bằng /admin/products
 */
@WebServlet("/admin/products/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 50     // 50 MB
)
public class AdminProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductController productController;
    private AuthService authService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        productController = new ProductController();
        authService = new AuthService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check admin authorization
        User user = checkAdminAuthorization(request, response);
        if (user == null) return;

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/admin/products - List all products
            Map<String, Object> result = productController.getProducts(request, response);
            sendJsonResponse(response, result);
        } else {
            // GET /api/admin/products/{id} - Get product by ID
            try {
                int productId = Integer.parseInt(pathInfo.substring(1));
                Map<String, Object> result = productController.getProductById(request, response, productId);
                sendJsonResponse(response, result);
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check admin authorization
        User user = checkAdminAuthorization(request, response);
        if (user == null) return;

        // POST /api/admin/products - Create new product
        Map<String, Object> result = productController.addProduct(request, response);
        sendJsonResponse(response, result);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check admin authorization
        User user = checkAdminAuthorization(request, response);
        if (user == null) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        // PUT /api/admin/products/{id} - Update product
        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            Map<String, Object> result = productController.updateProduct(request, response, productId);
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check admin authorization
        User user = checkAdminAuthorization(request, response);
        if (user == null) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        // DELETE /api/admin/products/{id} - Delete product
        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            Map<String, Object> result = productController.deleteProduct(request, response, productId);
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
        }
    }

    private User checkAdminAuthorization(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Get user from authentication token
        User user = authService.getUserFromRequest(request);
        
        // Check if user exists and is admin
        if (user == null || !user.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return null;
        }
        
        return user;
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendJsonResponse(response, error);
    }
} 