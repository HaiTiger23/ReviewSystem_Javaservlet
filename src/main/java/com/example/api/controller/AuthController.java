package com.example.api.controller;

import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.example.api.util.JwtUtil;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controller xử lý các yêu cầu liên quan đến xác thực người dùng
 */
public class AuthController {
    private AuthService authService;
    private static final long JWT_EXPIRATION = 86400000; // 24 giờ
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    /**
     * Xử lý yêu cầu đăng ký
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> register(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp dữ liệu đăng ký");
                return result;
            }
            
            String name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : null;
            String password = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : null;
            String confirmPassword = jsonRequest.has("confirmPassword") ? jsonRequest.get("confirmPassword").getAsString() : null;
            
            // Kiểm tra dữ liệu đầu vào
            if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng điền đầy đủ thông tin");
                return result;
            }
            
            // Kiểm tra mật khẩu khớp nhau
            if (!password.equals(confirmPassword)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Mật khẩu không khớp nhau");
                return result;
            }
            
            // Đăng ký người dùng
            User user = authService.register(name, email, password);
            
            if (user != null) {
                // Tạo JWT token
                String token = JwtUtil.generateToken(user.getId());
                
                // Trả về thông tin người dùng và token
                Map<String, Object> userMap = userToMap(user);
                result.put("user", userMap);
                result.put("token", token);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                result.put("error", "Email đã tồn tại");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu đăng nhập
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> login(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp thông tin đăng nhập");
                return result;
            }
            
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : null;
            String password = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : null;
            
            // Kiểm tra dữ liệu đầu vào
            if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng điền đầy đủ thông tin");
                return result;
            }
            
            // Đăng nhập
            User user = authService.login(email, password);
            
            if (user != null) {
                // Tạo JWT token
                String token = JwtUtil.generateToken(user.getId());
                
                // Trả về thông tin người dùng và token
                Map<String, Object> userMap = userToMap(user);
                result.put("user", userMap);
                result.put("token", token);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Email hoặc mật khẩu không đúng");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu đăng nhập bằng Google/Email
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> loginWithProvider(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp thông tin đăng nhập");
                return result;
            }
            
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : null;
            String name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            String avatar = jsonRequest.has("avatar") ? jsonRequest.get("avatar").getAsString() : null;
            String provider = jsonRequest.has("provider") ? jsonRequest.get("provider").getAsString() : null;
            
            // Kiểm tra dữ liệu đầu vào
            if (email == null || email.trim().isEmpty() ||
                name == null || name.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp đầy đủ thông tin");
                return result;
            }
            
            // Đăng nhập hoặc tạo tài khoản mới
            User user = authService.loginWithProvider(email, name, avatar);
            
            if (user != null) {
                // Tạo JWT token
                String token = JwtUtil.generateToken(user.getId());
                
                // Trả về thông tin người dùng và token
                Map<String, Object> userMap = userToMap(user);
                result.put("user", userMap);
                result.put("token", token);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("error", "Có lỗi xảy ra khi xử lý đăng nhập");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu quên mật khẩu
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> forgotPassword(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp email");
                return result;
            }
            
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : null;
            
            // Kiểm tra dữ liệu đầu vào
            if (email == null || email.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng nhập email");
                return result;
            }
            
            // Tạo token khôi phục mật khẩu
            String token = authService.forgotPassword(email);
            
            if (token != null) {
                // Trong thực tế, bạn sẽ gửi email chứa link khôi phục mật khẩu
                // Ví dụ: "http://yourdomain.com/reset-password?token=" + token
                
                result.put("success", true);
                result.put("message", "Email đặt lại mật khẩu đã được gửi");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("error", "Không tìm thấy tài khoản với email này");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu đặt lại mật khẩu
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> resetPassword(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp thông tin đặt lại mật khẩu");
                return result;
            }
            
            String token = jsonRequest.has("token") ? jsonRequest.get("token").getAsString() : null;
            String newPassword = jsonRequest.has("newPassword") ? jsonRequest.get("newPassword").getAsString() : null;
            
            // Kiểm tra dữ liệu đầu vào
            if (token == null || token.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp đầy đủ thông tin");
                return result;
            }
            
            // Đặt lại mật khẩu
            boolean success = authService.resetPassword(token, newPassword);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Đặt lại mật khẩu thành công");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Token không hợp lệ hoặc đã hết hạn");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu đăng xuất
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Yêu cầu xác thực");
                return result;
            }
            
            // Hủy session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            result.put("success", true);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu lấy thông tin người dùng hiện tại
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Yêu cầu xác thực");
                return result;
            }
            
            // Lấy token từ header
            String token = authHeader.substring(7);
            
            try {
                // Giải mã token để lấy thông tin người dùng
                Integer userId = JwtUtil.getUserIdFromToken(token);
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    result.put("error", "Token không hợp lệ hoặc đã hết hạn");
                    return result;
                }
                
                // Lấy thông tin người dùng từ database
                User user = authService.getUserById(userId);
                
                if (user != null) {
                    // Lấy số lượng đánh giá của người dùng
                    int reviewCount = authService.getUserReviewCount(userId);
                    
                    // Lấy số lượng bookmark của người dùng
                    int bookmarkCount = authService.getUserBookmarkCount(userId);
                    
                    // Định dạng ngày tham gia
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String joinDate = dateFormat.format(user.getCreatedAt() != null ? user.getCreatedAt() : new Date());
                    
                    // Tạo thông tin người dùng mở rộng
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("name", user.getName());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "/images/default-avatar.jpg");
                    userInfo.put("joinDate", joinDate);
                    userInfo.put("reviewCount", reviewCount);
                    userInfo.put("bookmarkCount", bookmarkCount);
                    
                    result = userInfo;
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    result.put("error", "Không tìm thấy thông tin người dùng");
                }
                
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Token không hợp lệ hoặc đã hết hạn");
                return result;
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu đổi mật khẩu
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> changePassword(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Yêu cầu xác thực");
                return result;
            }
            
            // Lấy token từ header
            String token = authHeader.substring(7);
            
            try {
                // Giải mã token để lấy thông tin người dùng
                Integer userId = JwtUtil.getUserIdFromToken(token);
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    result.put("error", "Token không hợp lệ hoặc đã hết hạn");
                    return result;
                }
                
                // Kiểm tra json request có tồn tại
                if (jsonRequest == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Vui lòng cung cấp dữ liệu đổi mật khẩu");
                    return result;
                }
                
                String currentPassword = jsonRequest.has("currentPassword") ? jsonRequest.get("currentPassword").getAsString() : null;
                String newPassword = jsonRequest.has("newPassword") ? jsonRequest.get("newPassword").getAsString() : null;
                String confirmPassword = jsonRequest.has("confirmPassword") ? jsonRequest.get("confirmPassword").getAsString() : null;
                
                // Kiểm tra dữ liệu đầu vào
                if (currentPassword == null || currentPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                    
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Vui lòng điền đầy đủ thông tin");
                    return result;
                }
                
                // Kiểm tra mật khẩu khớp nhau
                if (!newPassword.equals(confirmPassword)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Mật khẩu không khớp nhau");
                    return result;
                }
                
                // Đổi mật khẩu
                boolean success = authService.changePassword(userId, currentPassword, newPassword);
                
                if (success) {
                    result.put("success", true);
                    result.put("message", "Đổi mật khẩu thành công");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Mật khẩu hiện tại không đúng");
                }
                
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Token không hợp lệ hoặc đã hết hạn");
                return result;
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Cập nhật thông tin người dùng
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> updateProfile(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Chưa đăng nhập");
                return result;
            }
            
            String token = authHeader.substring(7);
            Integer userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Token không hợp lệ");
                return result;
            }
            
            // Kiểm tra dữ liệu đầu vào
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp thông tin cần cập nhật");
                return result;
            }
            
            String name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            String password = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : null;
            String confirmPassword = jsonRequest.has("confirmPassword") ? jsonRequest.get("confirmPassword").getAsString() : null;
            
            // Validate dữ liệu
            if (name == null || name.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Tên không được để trống");
                return result;
            }
            
            // Nếu có cập nhật mật khẩu
            if (password != null) {
                if (password.length() < 6) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Mật khẩu phải có ít nhất 6 ký tự");
                    return result;
                }
                
                if (confirmPassword == null || !confirmPassword.equals(password)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("error", "Mật khẩu xác nhận không khớp");
                    return result;
                }
            }
            
            // Cập nhật thông tin người dùng
            boolean success = authService.updateProfile(userId, name, password);
            
            if (success) {
                // Lấy thông tin người dùng sau khi cập nhật
                User updatedUser = authService.getUserById(userId);
                
                // Thành công
                response.setStatus(HttpServletResponse.SC_OK);
                result.put("message", "Cập nhật thông tin thành công");
                result.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "name", updatedUser.getName(),
                    "email", updatedUser.getEmail()
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("error", "Không thể cập nhật thông tin");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Chuyển đổi đối tượng User thành DTO (Data Transfer Object)
     * 
     * @param user Đối tượng User
     * @return Map chứa thông tin người dùng
     */
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", user.getId());
        userDto.put("name", user.getName());
        userDto.put("email", user.getEmail());
        userDto.put("avatar", user.getAvatar());
        return userDto;
    }
}
