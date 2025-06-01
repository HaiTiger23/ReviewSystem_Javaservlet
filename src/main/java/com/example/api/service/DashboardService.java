package com.example.api.service;

import com.example.api.dao.*;
import java.util.*;

/**
 * Service xử lý logic thống kê cho trang dashboard của admin
 */
public class DashboardService {
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Tổng số sản phẩm, người dùng, đánh giá, đơn hàng
            stats.put("totalProducts", productDAO.countTotalProducts());
            stats.put("totalUsers", userDAO.countTotalUsers());
            stats.put("totalReviews", reviewDAO.countTotalReviews());
            
            
            // Sản phẩm gần đây
            stats.put("recentProducts", productDAO.getRecentProducts(5));
            
            // Đánh giá gần đây
            stats.put("recentReviews", reviewDAO.getRecentReviews(5));
            
            // Thống kê sản phẩm theo danh mục
            stats.put("productCategories", productDAO.getProductCountByCategory());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy dữ liệu thống kê: " + e.getMessage());
        }
        
        return stats;
    }
    
}
