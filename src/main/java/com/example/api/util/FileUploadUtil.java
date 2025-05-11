package com.example.api.util;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Lớp tiện ích để xử lý tải lên file
 */
public class FileUploadUtil {
    private static final String UPLOAD_DIR = "/Users/macbook/Project/JavaProject/ReviewSystem/uploads";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");


    /**
     * Lưu file tải lên vào thư mục chỉ định
     * 
     * @param part Part chứa file tải lên
     * @param subDir Thư mục con (ví dụ: "products", "avatars")
     * @return Đường dẫn tương đối đến file đã lưu, hoặc null nếu có lỗi
     */
    public static String saveFile(Part part, String subDir) throws IOException {
        // Lấy tên file gốc
        String fileName = getSubmittedFileName(part);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Lấy phần mở rộng của file
        String fileExt = getFileExtension(fileName);
        if (fileExt == null || fileExt.isEmpty()) {
            return null;
        }
        if (!ALLOWED_EXTENSIONS.contains(fileExt.toLowerCase())) {
            throw new IllegalArgumentException("Định dạng file không được phép");
        }
        // Tạo tên file mới để tránh trùng lặp
        String newFileName = UUID.randomUUID().toString() + "." + fileExt;
        
        // Tạo đường dẫn đến thư mục lưu trữ
        String uploadPath = System.getProperty("catalina.base") + "/webapps/ROOT" + UPLOAD_DIR + "/" + subDir;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Lưu file
        Path filePath = Paths.get(uploadPath, newFileName);
        part.write(filePath.toString());
        
        // Trả về đường dẫn tương đối
        return UPLOAD_DIR + "/" + subDir + "/" + newFileName;
    }
    
    /**
     * Lấy tên file từ Part
     * 
     * @param part Part chứa file
     * @return Tên file
     */
    private static String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return "";
    }
    
    /**
     * Lấy phần mở rộng của file
     * 
     * @param fileName Tên file
     * @return Phần mở rộng của file
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
