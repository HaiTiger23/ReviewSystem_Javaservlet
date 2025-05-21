package com.example.api.servlet;

import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.example.api.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/users/*")
public class AdminUserServlet extends HttpServlet {
    private final UserService userService;
    private final AuthService authService;
    private final Gson gson;

    public AdminUserServlet() {
        this.userService = new UserService();
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Kiểm tra quyền admin
        User currentUser = authService.getUserFromRequest(request);
        if (currentUser == null || !currentUser.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        try {
            // Lấy danh sách người dùng với phân trang
            int page = getIntParameter(request, "page", 1);
            int limit = getIntParameter(request, "limit", 10);
            String search = request.getParameter("search");

            Map<String, Object> result = userService.getAllUsers(page, limit, search);
            sendJsonResponse(response, result);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Kiểm tra quyền admin
        User currentUser = authService.getUserFromRequest(request);
        if (currentUser == null || !currentUser.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID người dùng");
            return;
        }

        try {
            // Lấy ID người dùng từ URL
            int userId = Integer.parseInt(pathInfo.substring(1));
            
            // Đọc dữ liệu JSON từ request body
            JsonObject data = gson.fromJson(request.getReader(), JsonObject.class);
            
            // Validate dữ liệu
            if (!data.has("email") || data.get("email").getAsString().trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email không được để trống");
                return;
            }

            // Tạo đối tượng User từ dữ liệu
            User user = new User();
            user.setId(userId);
            user.setEmail(data.get("email").getAsString().trim());
            user.setName(data.has("name") ? data.get("name").getAsString().trim() : null);
            
            // Xử lý role
            if (data.has("role")) {
                String roleStr = data.get("role").getAsString().trim().toUpperCase();
                try {
                    user.setRole(User.Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Role không hợp lệ");
                    return;
                }
            }
            
            // Cập nhật thông tin người dùng
            boolean success = userService.updateUser(user);
            
            if (success) {
                User updatedUser = userService.getUserById(userId);
                sendJsonResponse(response, Map.of(
                    "success", true,
                    "message", "Cập nhật thông tin người dùng thành công",
                    "user", updatedUser
                ));
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy người dùng");
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID người dùng không hợp lệ");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    /**
     * Lấy giá trị tham số kiểu int từ request
     */
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return defaultValue;
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