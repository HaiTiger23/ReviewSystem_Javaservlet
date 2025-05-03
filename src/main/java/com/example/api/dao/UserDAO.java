package com.example.api.dao;

import com.example.api.model.User;
import com.example.api.model.User.Role;
import com.example.api.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lớp Data Access Object cho User, cung cấp các phương thức để tương tác với cơ sở dữ liệu
 */
public class UserDAO {

    /**
     * Đăng ký người dùng mới
     * 
     * @param user thông tin người dùng đăng ký
     * @return true nếu đăng ký thành công, false nếu thất bại
     */
    public boolean register(User user) {
        String sql = "INSERT INTO users (name, email, password, avatar, role) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword()); // Lưu ý: Mật khẩu nên được mã hóa trước khi lưu
            stmt.setString(4, user.getAvatar());
            stmt.setString(5, user.getRole().toString());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Kiểm tra đăng nhập của người dùng
     * 
     * @param email email người dùng
     * @param password mật khẩu người dùng
     * @return User nếu đăng nhập thành công, null nếu thất bại
     */
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // Kiểm tra mật khẩu (nên sử dụng thư viện mã hóa để so sánh)
                    if (storedPassword.equals(password)) { // Đây chỉ là so sánh đơn giản, trong thực tế cần sử dụng BCrypt hoặc tương tự
                        return mapResultSetToUser(rs);
                    }
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Đăng nhập bằng Google/Email
     * 
     * @param email email người dùng
     * @param name tên người dùng
     * @param avatar ảnh đại diện (có thể null)
     * @return User nếu thành công, null nếu thất bại
     */
    public User loginWithProvider(String email, String name, String avatar) {
        // Kiểm tra xem người dùng đã tồn tại chưa
        User existingUser = findByEmail(email);
        
        if (existingUser != null) {
            return existingUser;
        } else {
            // Tạo người dùng mới
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setAvatar(avatar);
            newUser.setPassword(UUID.randomUUID().toString()); // Tạo mật khẩu ngẫu nhiên
            newUser.setRole(Role.USER);
            
            if (register(newUser)) {
                return newUser;
            } else {
                return null;
            }
        }
    }
    
    /**
     * Tạo token để khôi phục mật khẩu
     * 
     * @param email email người dùng
     * @return token khôi phục nếu thành công, null nếu thất bại
     */
    public String createPasswordResetToken(String email) {
        User user = findByEmail(email);
        
        if (user != null) {
            String token = UUID.randomUUID().toString();
            
            // Trong thực tế, bạn sẽ lưu token này vào bảng riêng hoặc thêm cột vào bảng users
            // Ở đây, chúng ta giả định có bảng password_reset_tokens
            String sql = "INSERT INTO password_reset_tokens (user_id, token, expiry_date) VALUES (?, ?, ?)";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, user.getId());
                stmt.setString(2, token);
                // Hết hạn sau 1 giờ
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 3600000));
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    return token;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * Đặt lại mật khẩu bằng token
     * 
     * @param token token khôi phục
     * @param newPassword mật khẩu mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean resetPassword(String token, String newPassword) {
        // Kiểm tra token có hợp lệ không
        String findTokenSql = "SELECT user_id FROM password_reset_tokens WHERE token = ? AND expiry_date > ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findTokenSql)) {
            
            stmt.setString(1, token);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    // Cập nhật mật khẩu
                    String updatePasswordSql = "UPDATE users SET password = ? WHERE id = ?";
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePasswordSql)) {
                        updateStmt.setString(1, newPassword); // Nên mã hóa mật khẩu trước khi lưu
                        updateStmt.setInt(2, userId);
                        
                        int rowsAffected = updateStmt.executeUpdate();
                        
                        // Xóa token đã sử dụng
                        if (rowsAffected > 0) {
                            String deleteTokenSql = "DELETE FROM password_reset_tokens WHERE token = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteTokenSql)) {
                                deleteStmt.setString(1, token);
                                deleteStmt.executeUpdate();
                            }
                            return true;
                        }
                    }
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tìm người dùng theo email
     * 
     * @param email email cần tìm
     * @return User nếu tìm thấy, null nếu không
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tìm người dùng theo ID
     * 
     * @param id ID cần tìm
     * @return User nếu tìm thấy, null nếu không
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách tất cả người dùng
     * 
     * @return Danh sách người dùng
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Cập nhật thông tin người dùng
     * 
     * @param user thông tin người dùng cần cập nhật
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, avatar = ?, role = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getAvatar());
            stmt.setString(4, user.getRole().toString());
            stmt.setInt(5, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật mật khẩu người dùng
     * 
     * @param userId ID người dùng
     * @param newPassword mật khẩu mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword); // Nên mã hóa mật khẩu trước khi lưu
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xóa người dùng
     * 
     * @param userId ID người dùng cần xóa
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy số lượng đánh giá của người dùng
     * 
     * @param userId ID người dùng
     * @return Số lượng đánh giá
     */
    public int getReviewCount(int userId) {
        String sql = "SELECT COUNT(*) AS count FROM reviews WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Lấy số lượng bookmark của người dùng
     * 
     * @param userId ID người dùng
     * @return Số lượng bookmark
     */
    public int getBookmarkCount(int userId) {
        String sql = "SELECT COUNT(*) AS count FROM bookmarks WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Chuyển đổi ResultSet thành đối tượng User
     * 
     * @param rs ResultSet từ truy vấn
     * @return Đối tượng User
     * @throws SQLException nếu có lỗi khi truy cập dữ liệu
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAvatar(rs.getString("avatar"));
        
        // Lấy ngay tạo và cập nhật
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        String roleStr = rs.getString("role");
        try {
            user.setRole(Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            // Mặc định là USER nếu không tìm thấy role hợp lệ
            user.setRole(Role.USER);
        }
        
        return user;
    }
}
