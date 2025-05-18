package com.example.api.controller;

import com.example.api.service.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet xử lý các yêu cầu API liên quan đến xác thực người dùng
 */
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AuthController authController;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        authController = new AuthController();
        gson = new Gson();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Đọc dữ liệu JSON từ request body
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        
        // Parse JSON từ request body
        JsonObject jsonRequest = null;
        if (buffer.length() > 0) {
            try {
                jsonRequest = JsonParser.parseString(buffer.toString()).getAsJsonObject();
            } catch (Exception e) {
                // Không phải JSON hợp lệ
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(response, Map.of("error", "Invalid JSON format"));
                return;
            }
        }
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        
        // Xử lý các endpoint khác nhau
        Map<String, Object> result;
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result = Map.of("error", "Endpoint không hợp lệ");
            } else if (pathInfo.equals("/register")) {
                result = authController.register(jsonRequest, request, response);
            } else if (pathInfo.equals("/login")) {
                result = authController.login(jsonRequest, request, response);
            } else if (pathInfo.equals("/login-provider")) {
                result = authController.loginWithProvider(jsonRequest, request, response);
            } else if (pathInfo.equals("/forgot-password")) {
                result = authController.forgotPassword(jsonRequest, request, response);
            } else if (pathInfo.equals("/reset-password")) {
                result = authController.resetPassword(jsonRequest, request, response);
            } else if (pathInfo.equals("/logout")) {
                result = authController.logout(request, response);
            } else if (pathInfo.equals("/change-password")) {
                result = authController.changePassword(jsonRequest, request, response);
            } else if (pathInfo.equals("/update-profile")) {
                result = authController.updateProfile(jsonRequest, request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result = Map.of("error", "Endpoint không hợp lệ");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Trả về kết quả dưới dạng JSON
        sendJsonResponse(response, result);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        
        // Xử lý các endpoint cho phương thức GET
        Map<String, Object> result;
        
        try {
            if (pathInfo != null && pathInfo.equals("/logout")) {
                doPost(request, response);
                return;
            } else if (pathInfo != null && pathInfo.equals("/me")) {
                result = authController.getCurrentUser(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                result = Map.of("error", "Phương thức không được hỗ trợ");
            }
            
            // Trả về kết quả dưới dạng JSON
            sendJsonResponse(response, result);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendJsonResponse(response, Map.of("error", "Lỗi server: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }
}
