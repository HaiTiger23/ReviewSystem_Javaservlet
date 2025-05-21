package com.example.api.service;

import com.example.api.dao.CategoryDAO;
import com.example.api.model.Category;
import com.example.api.util.SlugUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {
    private CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Lấy danh sách danh mục với phân trang và tìm kiếm
     */
    public Map<String, Object> getCategories(int page, int limit, String search) {
        Map<String, Object> result = new HashMap<>();
        
        // Lấy danh sách danh mục từ DAO
        List<Category> categories = categoryDAO.getAllCategories();
        
        // Tính toán phân trang
        int total = categories.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);
        
        // Cắt danh sách theo phân trang
        List<Category> pagedCategories = categories.subList(start, end);
        
        // Tạo thông tin phân trang
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", totalPages);
        
        result.put("categories", pagedCategories);
        result.put("pagination", pagination);
        
        return result;
    }

    /**
     * Lấy thông tin chi tiết danh mục
     */
    public Category getCategoryById(int categoryId) {
        return categoryDAO.getCategoryById(categoryId);
    }

    /**
     * Thêm danh mục mới
     */
    public Category addCategory(String name) {
        // Tạo slug từ tên
        String slug = SlugUtil.createSlug(name);
        
        // Tạo đối tượng Category
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        
        // Thêm vào database
        int categoryId = categoryDAO.addCategory(category);
        if (categoryId > 0) {
            category.setId(categoryId);
            return category;
        }
        
        return null;
    }

    /**
     * Cập nhật thông tin danh mục
     */
    public boolean updateCategory(int categoryId, String name) {
        // Kiểm tra danh mục tồn tại
        Category category = categoryDAO.getCategoryById(categoryId);
        if (category == null) {
            return false;
        }
        
        // Cập nhật thông tin
        category.setName(name);
        category.setSlug(SlugUtil.createSlug(name));
        
        // Lưu vào database
        return categoryDAO.updateCategory(category);
    }

    /**
     * Xóa danh mục
     */
    public boolean deleteCategory(int categoryId) {
        // Kiểm tra danh mục tồn tại
        if (!categoryDAO.isCategoryExists(categoryId)) {
            return false;
        }
        
        // Kiểm tra có sản phẩm không
        if (categoryDAO.hasCategoryProducts(categoryId)) {
            return false;
        }
        
        // Xóa danh mục
        return categoryDAO.deleteCategory(categoryId);
    }
} 