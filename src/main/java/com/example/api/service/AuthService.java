package com.example.api.service;

import com.example.api.dao.UserDAO;
import com.example.api.model.User;
import com.example.api.util.PasswordUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Lớp dịch vụ xử lý các chức năng xác thực người dùng
 */
public class AuthService {
    private UserDAO userDAO;
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    
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
        
        // Mã hóa mật khẩu bằng BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
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
            String storedPassword = user.getPassword();
            
            // Kiểm tra xem mật khẩu có phải định dạng BCrypt không
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // Mật khẩu đã được mã hóa bằng BCrypt
                if (BCrypt.checkpw(password, storedPassword)) {
                    return user;
                }
            } else {
                // Mật khẩu cũ sử dụng SHA-256
                if (PasswordUtil.checkPassword(password, storedPassword)) {
                    // Nếu đăng nhập thành công với mật khẩu cũ, cập nhật sang BCrypt
                    String newHashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    if (userDAO.updatePassword(user.getId(), newHashedPassword)) {
                        user.setPassword(newHashedPassword);
                        return user;
                    }
                }
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
        // Mã hóa mật khẩu mới bằng BCrypt
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        return userDAO.resetPassword(token, hashedPassword);
    }
    
    /**
     * Cập nhật thông tin người dùng
     * 
     * @param userId ID người dùng
     * @param name Tên mới
     * @param password Mật khẩu mới (có thể null nếu không đổi)
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public boolean updateProfile(Integer userId, String name, String password) {
        try {
            // Lấy thông tin người dùng hiện tại
            User currentUser = userDAO.getUserById(userId);
            if (currentUser == null) {
                return false;
            }
            
            // Cập nhật thông tin
            currentUser.setName(name);
            
            // Nếu có cập nhật mật khẩu
            if (password != null) {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                currentUser.setPassword(hashedPassword);
            }
            
            // Lưu vào database
            return userDAO.updateUser(currentUser);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật thông tin người dùng: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Lấy thông tin người dùng theo ID
     * 
     * @param userId ID người dùng
     * @return Đối tượng User hoặc null nếu không tìm thấy
     */
    public User getUserById(Integer userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), e);
            return null;
        }
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
        
        // Kiểm tra mật khẩu hiện tại bằng BCrypt
        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Mã hóa mật khẩu mới bằng BCrypt
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
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
