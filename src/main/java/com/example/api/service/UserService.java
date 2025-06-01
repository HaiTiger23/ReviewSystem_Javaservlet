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
    
    /**
     * Đổi mật khẩu người dùng (cho admin)
     * 
     * @param userId ID người dùng
     * @param newPassword Mật khẩu mới (đã được mã hoá)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updatePassword(int userId, String newPassword) {
        // Kiểm tra người dùng tồn tại
        User existingUser = userDAO.getUserById(userId);
        if (existingUser == null) {
            return false;
        }
        
        // Cập nhật mật khẩu
        return userDAO.updatePassword(userId, newPassword);
    }
    
    /**
     * Cập nhật trạng thái người dùng (khoá/mở khoá tài khoản)
     * 
     * @param userId ID người dùng
     * @param status Trạng thái mới (1: hoạt động, 0: bị khoá)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateStatus(int adminId, int userId, int status) {
        // Kiểm tra người dùng tồn tại
        User existingUserUpdate = userDAO.getUserById(userId);
        if (existingUserUpdate == null) {
            System.out.println("User not found");
            return false;
        }
        
        // Không cho phép tự khoá tài khoản của chính mình
        if (existingUserUpdate.getId() == adminId) {
            System.out.println("Cannot lock yourself :" + userId);
            return false;
        }
        
        // Cập nhật trạng thái
        boolean success = userDAO.updateStatus(userId, status);
        if (success) {
            System.out.println("Update status success");
        } else {
            System.out.println("Update status failed");
        }
        return success;
    }
} 