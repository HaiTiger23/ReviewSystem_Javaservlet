package com.example.api.service;

import com.example.api.dao.UserDAO;
import com.example.api.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Lấy danh sách người dùng với phân trang và tìm kiếm
     */
    public Map<String, Object> getAllUsers(int page, int limit, String search) {
        Map<String, Object> result = new HashMap<>();
        
        // Lấy danh sách người dùng từ DAO
        List<User> users = userDAO.findAll();
        
        // Tính toán phân trang
        int total = users.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);
        
        // Cắt danh sách theo phân trang
        List<User> pagedUsers = users.subList(start, end);
        
        // Tạo thông tin phân trang
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", totalPages);
        
        result.put("users", pagedUsers);
        result.put("pagination", pagination);
        
        return result;
    }

    /**
     * Lấy thông tin chi tiết người dùng
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public boolean updateUser(User user) {
        // Kiểm tra người dùng tồn tại
        User existingUser = userDAO.getUserById(user.getId());
        if (existingUser == null) {
            return false;
        }
        
        // Cập nhật thông tin
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        
        // Lưu vào database
        return userDAO.updateUser(existingUser);
    }
} 