package com.example.api.controller;

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
 * Servlet xử lý các yêu cầu API liên quan đến đánh giá sản phẩm
 */
public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReviewController reviewController;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        reviewController = new ReviewController();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        
        // Xử lý các endpoint khác nhau
        Map<String, Object> result;
        
        try {
            // Lấy danh sách đánh giá của sản phẩm: /api/product-reviews/{productId}
            if (servletPath.equals("/api/product-reviews") && pathInfo != null && pathInfo.matches("/\\d+")) {
                // Lấy ID sản phẩm từ URL
                int productId = Integer.parseInt(pathInfo.substring(1));
                
                // Lấy tham số phân trang
                int page = getIntParameter(request, "page", 1);
                int limit = getIntParameter(request, "limit", 10);
                String sort = request.getParameter("sort");
                
                // Lấy danh sách đánh giá
                result = reviewController.getProductReviews(productId, page, limit, sort, request, response);
            }
            // Lấy chi tiết đánh giá: /api/reviews/{reviewId}
            else if (servletPath.equals("/api/reviews") && pathInfo != null && pathInfo.matches("/\\d+")) {
                // Lấy ID đánh giá từ URL
                int reviewId = Integer.parseInt(pathInfo.substring(1));
                
                // Lấy chi tiết đánh giá
                // (chức năng này chưa được thực hiện trong ReviewController)
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                result = Map.of("error", "Chức năng này chưa được thực hiện");
            }
            else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Endpoint không hợp lệ");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(Map.of("error", "Có lỗi xảy ra: " + e.getMessage())));
                out.flush();
            }
            e.printStackTrace();
            return;
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(Map.of("error", "Có lỗi xảy ra: " + e.getMessage())));
                out.flush();
            }
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        
        // Lấy dữ liệu JSON từ request body
        JsonObject jsonRequest = null;
        try {
            BufferedReader reader = request.getReader();
            if (reader != null) {
                jsonRequest = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            // Không có dữ liệu JSON hoặc không phải là JSON
        }
        
        // Xử lý các endpoint khác nhau
        Map<String, Object> result;
        
        try {
            // Thêm đánh giá mới: /api/product-reviews/{productId}
          
        } catch (Exception e) {  if (servletPath.equals("/api/product-reviews") && pathInfo != null && pathInfo.matches("/\\d+")) {
                // Lấy ID sản phẩm từ URL
                int productId = Integer.parseInt(pathInfo.substring(1));
                
                // Thêm đánh giá mới
                result = reviewController.addReview(productId, jsonRequest, request, response);
            }
            // Đánh dấu đánh giá là hữu ích: /api/reviews/{reviewId}/helpful
            else if (servletPath.equals("/api/reviews") && pathInfo != null && pathInfo.matches("/\\d+/helpful")) {
                // Lấy ID đánh giá từ URL
                int reviewId = Integer.parseInt(pathInfo.split("/")[1]);
                
                // Đánh dấu đánh giá là hữu ích
                result = reviewController.markReviewHelpful(reviewId, jsonRequest, request, response);
            }
            else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Endpoint không hợp lệ");
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        
        // Lấy dữ liệu JSON từ request body
        JsonObject jsonRequest = null;
        try {
            BufferedReader reader = request.getReader();
            if (reader != null) {
                jsonRequest = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            // Không có dữ liệu JSON hoặc không phải là JSON
        }
        
        // Xử lý các endpoint khác nhau
        Map<String, Object> result;
        
        try {
            // Cập nhật đánh giá: /api/reviews/{reviewId}
            if (servletPath.equals("/api/reviews") && pathInfo != null && pathInfo.matches("/\\d+")) {
                // Lấy ID đánh giá từ URL
                int reviewId = Integer.parseInt(pathInfo.substring(1));
                
                // Cập nhật đánh giá
                result = reviewController.updateReview(reviewId, jsonRequest, request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Endpoint không hợp lệ");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        
        // Xử lý các endpoint khác nhau
        Map<String, Object> result;
        
        try {
            // Xóa đánh giá: /api/reviews/{reviewId}
            if (servletPath.equals("/api/reviews") && pathInfo != null && pathInfo.matches("/\\d+")) {
                // Lấy ID đánh giá từ URL
                int reviewId = Integer.parseInt(pathInfo.substring(1));
                
                // Xóa đánh giá
                result = reviewController.deleteReview(reviewId, request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Endpoint không hợp lệ");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Trả về kết quả dưới dạng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Lấy tham số kiểu int từ request
     * 
     * @param request Yêu cầu HTTP
     * @param paramName Tên tham số
     * @param defaultValue Giá trị mặc định
     * @return Giá trị tham số
     */
    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
