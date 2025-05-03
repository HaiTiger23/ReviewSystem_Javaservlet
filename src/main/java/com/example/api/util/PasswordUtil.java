package com.example.api.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Lớp tiện ích để mã hóa và kiểm tra mật khẩu
 */
public class PasswordUtil {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Mã hóa mật khẩu sử dụng SHA-256 với salt
     * 
     * @param password Mật khẩu cần mã hóa
     * @return Mật khẩu đã mã hóa (salt + hash)
     */
    public static String hashPassword(String password) {
        try {
            // Tạo salt ngẫu nhiên
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Mã hóa mật khẩu với salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Chuyển salt và hash thành chuỗi Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Kết hợp salt và hash
            return saltBase64 + ":" + hashBase64;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi khi mã hóa mật khẩu", e);
        }
    }
    
    /**
     * Kiểm tra mật khẩu có khớp với mật khẩu đã mã hóa không
     * 
     * @param password Mật khẩu cần kiểm tra
     * @param hashedPassword Mật khẩu đã mã hóa (salt + hash)
     * @return true nếu mật khẩu khớp, false nếu không
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        try {
            // Tách salt và hash
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String saltBase64 = parts[0];
            String hashBase64 = parts[1];
            
            // Chuyển chuỗi Base64 thành byte[]
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            
            // Mã hóa mật khẩu với salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] newHashedPassword = md.digest(password.getBytes());
            
            // So sánh hash
            String newHashBase64 = Base64.getEncoder().encodeToString(newHashedPassword);
            return hashBase64.equals(newHashBase64);
            
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }
}
