package com.example.api.service;

import com.example.api.dao.UserDAO;
import com.example.api.model.User;
import com.example.api.util.PasswordUtil;

/**
 * Lớp dịch vụ xử lý các chức năng xác thực người dùng
 */
public class AuthService {
    private UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Đăng ký người dùng mới
     * 
     * @param name tên người dùng
     * @param email email người dùng
     * @param password mật khẩu người dùng
     * @return User nếu đăng ký thành công, null nếu thất bại
     */
    public User register(String name, String email, String password) {
        // Kiểm tra email đã tồn tại chưa
        if (userDAO.findByEmail(email) != null) {
            return null; // Email đã tồn tại
        }
        
        // Mã hóa mật khẩu
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Tạo người dùng mới
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(User.Role.USER);
        
        // Đăng ký người dùng
        if (userDAO.register(user)) {
            return user;
        } else {
            return null;
        }
    }
    
    /**
     * Đăng nhập người dùng
     * 
     * @param email email người dùng
     * @param password mật khẩu người dùng
     * @return User nếu đăng nhập thành công, null nếu thất bại
     */
    public User login(String email, String password) {
        User user = userDAO.findByEmail(email);
        
        if (user != null) {
            // Kiểm tra mật khẩu
            if (PasswordUtil.checkPassword(password, user.getPassword())) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Đăng nhập bằng Google hoặc Email
     * 
     * @param email email người dùng
     * @param name tên người dùng
     * @param avatar ảnh đại diện
     * @return User nếu đăng nhập thành công, null nếu thất bại
     */
    public User loginWithProvider(String email, String name, String avatar) {
        return userDAO.loginWithProvider(email, name, avatar);
    }
    
    /**
     * Tạo token để khôi phục mật khẩu
     * 
     * @param email email người dùng
     * @return token khôi phục nếu thành công, null nếu thất bại
     */
    public String forgotPassword(String email) {
        return userDAO.createPasswordResetToken(email);
    }
    
    /**
     * Đặt lại mật khẩu bằng token
     * 
     * @param token token khôi phục
     * @param newPassword mật khẩu mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean resetPassword(String token, String newPassword) {
        // Mã hóa mật khẩu mới
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        return userDAO.resetPassword(token, hashedPassword);
    }
    
    /**
     * Lấy thông tin người dùng theo ID
     * 
     * @param userId ID người dùng
     * @return User nếu tìm thấy, null nếu không tìm thấy
     */
    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }
    
    /**
     * Đổi mật khẩu cho người dùng
     * 
     * @param userId ID người dùng
     * @param currentPassword mật khẩu hiện tại
     * @param newPassword mật khẩu mới
     * @return true nếu thành công, false nếu mật khẩu hiện tại không đúng hoặc có lỗi
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        // Lấy thông tin người dùng
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        // Kiểm tra mật khẩu hiện tại
        if (!PasswordUtil.checkPassword(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Mã hóa mật khẩu mới
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        // Cập nhật mật khẩu
        return userDAO.updatePassword(userId, hashedPassword);
    }
    
    /**
     * Lấy số lượng đánh giá của người dùng
     * 
     * @param userId ID người dùng
     * @return Số lượng đánh giá
     */
    public int getUserReviewCount(int userId) {
        return userDAO.getReviewCount(userId);
    }
    
    /**
     * Lấy số lượng bookmark của người dùng
     * 
     * @param userId ID người dùng
     * @return Số lượng bookmark
     */
    public int getUserBookmarkCount(int userId) {
        return userDAO.getBookmarkCount(userId);
    }
}
