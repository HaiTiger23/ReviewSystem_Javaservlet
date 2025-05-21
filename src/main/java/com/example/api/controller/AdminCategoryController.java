package com.example.api.controller;

import com.example.api.dao.CategoryDAO;
import com.example.api.model.Category;
import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.example.api.util.JwtUtil;
import com.example.api.util.SlugUtil;
import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý logic cho các API quản lý danh mục dành cho Admin
 */
public class AdminCategoryController {
    private CategoryDAO categoryDAO;
    private AuthService authService;
    private JwtUtil jwtUtil;
    
    public AdminCategoryController() {
        this.categoryDAO = new CategoryDAO();
        this.authService = new AuthService();
        this.jwtUtil = new JwtUtil();
    }
    
    /**
     * Lấy danh sách tất cả danh mục (admin view)
     */
    public Map<String, Object> getAllCategories(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        // Kiểm tra quyền admin
        User user = authService.getUserFromRequest(request);
        if (user == null || !user.isAdmin()) {
            result.put("error", "Bạn không có quyền truy cập");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return result;
        }
        
        List<Category> categories = categoryDAO.getAllCategories();
        List<Map<String, Object>> categoriesData = new ArrayList<>();
        
        for (Category category : categories) {
            categoriesData.add(categoryToMap(category));
        }
        
        result.put("categories", categoriesData);
        return result;
    }
    
    /**
     * Lấy thông tin danh mục theo ID (admin view)
     */
    public Map<String, Object> getCategoryById(int categoryId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        // Kiểm tra quyền admin
        User user = authService.getUserFromRequest(request);
        if (user == null || !user.isAdmin()) {
            result.put("error", "Bạn không có quyền truy cập");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return result;
        }
        
        Category category = categoryDAO.getCategoryById(categoryId);
        
        if (category != null) {
            result.put("category", categoryToMap(category));
        } else {
            result.put("error", "Không tìm thấy danh mục");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        return result;
    }
    
    /**
     * Thêm danh mục mới
     */
    public Map<String, Object> addCategory(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra quyền admin
            User user = authService.getUserFromRequest(request);
            if (user == null || !user.isAdmin()) {
                result.put("error", "Bạn không có quyền thực hiện hành động này");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return result;
            }
            
            // Parse thông tin danh mục từ JSON
            String name = jsonRequest.get("name").getAsString();
            Integer parentId = null;
            
            if (jsonRequest.has("parent_id") && !jsonRequest.get("parent_id").isJsonNull()) {
                parentId = jsonRequest.get("parent_id").getAsInt(); 
                
                // Kiểm tra danh mục cha có tồn tại không
                if (!categoryDAO.isCategoryExists(parentId)) {
                    result.put("error", "Danh mục cha không tồn tại");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return result;
                }
            }
            
            // Tạo slug từ tên danh mục
            String slug = SlugUtil.createSlug(name);
            
            // Tạo đối tượng danh mục
            Category category = new Category();
            category.setName(name);
            category.setSlug(slug);
            category.setParentId(parentId);
            
            // Thêm danh mục vào database
            int categoryId = categoryDAO.addCategory(category);
            
            if (categoryId > 0) {
                category.setId(categoryId);
                result.put("success", true);
                result.put("message", "Thêm danh mục thành công");
                result.put("category", categoryToMap(category));
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                result.put("error", "Không thể thêm danh mục");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            result.put("error", "Lỗi khi thêm danh mục: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return result;
    }
    
    /**
     * Cập nhật thông tin danh mục
     */
    public Map<String, Object> updateCategory(int categoryId, JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra quyền admin
            User user = authService.getUserFromRequest(request);
            if (user == null || !user.isAdmin()) {
                result.put("error", "Bạn không có quyền thực hiện hành động này");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return result;
            }
            
            // Kiểm tra danh mục có tồn tại không
            Category category = categoryDAO.getCategoryById(categoryId);
            if (category == null) {
                result.put("error", "Không tìm thấy danh mục");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return result;
            }
            
            // Parse thông tin danh mục từ JSON
            String name = jsonRequest.get("name").getAsString();
            Integer parentId = null;
            
            if (jsonRequest.has("parent_id") && !jsonRequest.get("parent_id").isJsonNull()) {
                parentId = jsonRequest.get("parent_id").getAsInt();
                
                // Kiểm tra danh mục cha có tồn tại không
                if (!categoryDAO.isCategoryExists(parentId)) {
                    result.put("error", "Danh mục cha không tồn tại");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return result;
                }
                
                // Kiểm tra không được chọn chính nó làm cha
                if (parentId == categoryId) {
                    result.put("error", "Không thể chọn chính danh mục này làm danh mục cha");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return result;
                }
            }
            
            // Tạo slug từ tên danh mục
            String slug = SlugUtil.createSlug(name);
            
            // Cập nhật thông tin danh mục
            category.setName(name);
            category.setSlug(slug);
            category.setParentId(parentId);
            
            // Cập nhật danh mục trong database
            boolean success = categoryDAO.updateCategory(category);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Cập nhật danh mục thành công");
                result.put("category", categoryToMap(category));
            } else {
                result.put("error", "Không thể cập nhật danh mục");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            result.put("error", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return result;
    }
    
    /**
     * Xóa danh mục
     */
    public Map<String, Object> deleteCategory(int categoryId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra quyền admin
            User user = authService.getUserFromRequest(request);
            if (user == null || !user.isAdmin()) {
                result.put("error", "Bạn không có quyền thực hiện hành động này");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return result;
            }
            
            // Kiểm tra danh mục có tồn tại không
            if (!categoryDAO.isCategoryExists(categoryId)) {
                result.put("error", "Không tìm thấy danh mục");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return result;
            }
            
            // Kiểm tra danh mục có sản phẩm không
            if (categoryDAO.hasCategoryProducts(categoryId)) {
                result.put("error", "Không thể xóa danh mục đã có sản phẩm");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Xóa danh mục
            boolean success = categoryDAO.deleteCategory(categoryId);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Xóa danh mục thành công");
            } else {
                result.put("error", "Không thể xóa danh mục");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            result.put("error", "Lỗi khi xóa danh mục: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return result;
    }
    
    /**
     * Chuyển đối tượng Category thành Map
     */
    private Map<String, Object> categoryToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("slug", category.getSlug());
        map.put("parent_id", category.getParentId());
        map.put("created_at", category.getCreatedAt());
        map.put("updated_at", category.getUpdatedAt());
        return map;
    }
} 