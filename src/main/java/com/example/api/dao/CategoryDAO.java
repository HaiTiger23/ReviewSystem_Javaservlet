package com.example.api.dao;

import com.example.api.model.Category;
import com.example.api.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xu1eed lu00fd truy vu1ea5n du1eef liu1ec7u cho danh mu1ee5c
 */
public class CategoryDAO {
    
    /**
     * Kiu1ec3m tra xem danh mu1ee5c cu00f3 tu1ed3n tu1ea1i hay khu00f4ng
     * 
     * @param categoryId ID cu1ee7a danh mu1ee5c cu1ea7n kiu1ec3m tra
     * @return true nu1ebfu danh mu1ee5c tu1ed3n tu1ea1i, false nu1ebfu khu00f4ng
     */
    public boolean isCategoryExists(int categoryId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lu1ed7i khi kiu1ec3m tra danh mu1ee5c: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Lu1ea5y danh su00e1ch tu1ea5t cu1ea3 danh mu1ee5c
     * 
     * @return Danh su00e1ch danh mu1ee5c
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, slug, parent_id, created_at, updated_at FROM categories ORDER BY name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setParentId(rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null);
                category.setCreatedAt(rs.getTimestamp("created_at"));
                category.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                categories.add(category);
            }
            
        } catch (SQLException e) {
            System.err.println("Lu1ed7i khi lu1ea5y danh su00e1ch danh mu1ee5c: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Thêm danh mục mới
     * 
     * @param category Đối tượng danh mục cần thêm
     * @return ID của danh mục nếu thành công, -1 nếu thất bại
     */
    public int addCategory(Category category) {
        String sql = "INSERT INTO categories (name, slug, parent_id) VALUES (?, ?, ?)"; 
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getSlug());
            
            if (category.getParentId() != null) {
                stmt.setInt(3, category.getParentId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Cập nhật thông tin danh mục
     * 
     * @param category Đối tượng danh mục cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, slug = ?, parent_id = ? WHERE id = ?"; 
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getSlug());
            
            if (category.getParentId() != null) {
                stmt.setInt(3, category.getParentId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(4, category.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Xóa danh mục theo ID
     * 
     * @param categoryId ID của danh mục cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteCategory(int categoryId) {
        // Kiểm tra xem danh mục có sản phẩm không
        if (hasCategoryProducts(categoryId)) {
            return false; // Không xóa danh mục có sản phẩm
        }
        
        String sql = "DELETE FROM categories WHERE id = ?"; 
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Kiểm tra xem danh mục có sản phẩm không
     * 
     * @param categoryId ID của danh mục cần kiểm tra
     * @return true nếu danh mục có sản phẩm, false nếu không
     */
    public boolean hasCategoryProducts(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?"; 
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra sản phẩm của danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Lấy thông tin danh mục theo ID
     * 
     * @param categoryId ID của danh mục cần lấy thông tin
     * @return Đối tượng danh mục, null nếu không tìm thấy
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT id, name, slug, parent_id, created_at, updated_at FROM categories WHERE id = ?"; 
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setName(rs.getString("name"));
                    category.setSlug(rs.getString("slug"));
                    category.setParentId(rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null);
                    category.setCreatedAt(rs.getTimestamp("created_at"));
                    category.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    return category;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
