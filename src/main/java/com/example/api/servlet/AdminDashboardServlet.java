package com.example.api.servlet;

import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.example.api.service.DashboardService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/dashboard/*")
public class AdminDashboardServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Kiểm tra quyền admin
        User currentUser = authService.getUserFromRequest(request);
        if (currentUser == null || !currentUser.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/stats")) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "API không tồn tại");
            return;
        }

        try {
            // Lấy dữ liệu thống kê
            Map<String, Object> stats = dashboardService.getDashboardStats();
            
            // Trả về response
            sendJsonResponse(response, Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        sendJsonResponse(response, Map.of(
            "success", false,
            "message", message
        ));
    }
}
