package com.example.api.controller;

import com.example.api.model.Product;
import com.example.api.model.ProductSpecification;
import com.example.api.model.User;
import com.example.api.service.ProductService;
import com.example.api.service.AuthService;
import com.example.api.dao.BookmarkDAO;
import com.example.api.dao.CategoryDAO;
import com.example.api.util.FileUploadUtil;
import com.example.api.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Controller xử lý các yêu cầu liên quan đến sản phẩm
 */
public class ProductController {
    private ProductService productService;
    private AuthService authService;
    private CategoryDAO categoryDAO;
    private BookmarkDAO bookmarkDAO;
    private Gson gson;
    
    public ProductController() {
        this.productService = new ProductService();
        this.authService = new AuthService();
        this.categoryDAO = new CategoryDAO();
        this.bookmarkDAO = new BookmarkDAO();
        this.gson = new Gson();
    }
    
    /**
     * Xử lý yêu cầu lấy danh sách sản phẩm
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getProducts(HttpServletRequest request, HttpServletResponse response) {
        // Lấy các tham số từ request
        int page = getIntParameter(request, "page", 1);
        int limit = getIntParameter(request, "limit", 10);
        String categorySlug = request.getParameter("category");
        String search = request.getParameter("search");
        String sort = request.getParameter("sort");
        
        // Chuyển đổi category slug thành category ID nếu cần
        Integer categoryId = null;
        if (categorySlug != null && !categorySlug.isEmpty()) {
            // Trong thực tế, bạn sẽ cần một CategoryDAO để lấy ID từ slug
            // Ở đây, chúng ta giả định categoryId = 1 cho mục đích minh họa
            categoryId = 1;
        }
        
        // Lấy danh sách sản phẩm
        Map<String, Object> result = productService.getProducts(page, limit, categoryId, search, sort);
        
        // Định dạng giá tiền cho các sản phẩm
        formatProductPrices(result);
        
        response.setStatus(HttpServletResponse.SC_OK);
        return result;
    }
    
    /**
     * Xử lý yêu cầu lấy chi tiết sản phẩm
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param productId ID sản phẩm
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getProductById(HttpServletRequest request, HttpServletResponse response, int productId) {
        Map<String, Object> result = new HashMap<>();
        
        // Lấy ID người dùng từ token nếu có
        Integer userId = getUserIdFromToken(request);
        
        // Lấy thông tin sản phẩm
        Product product = productService.getProductById(productId, userId);
        
        if (product != null) {
            // Chuyển đổi Product thành Map
            result.put("product", productToMap(product));
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            result.put("error", "Không tìm thấy sản phẩm");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu thêm sản phẩm mới
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> addProduct(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            Integer userId = getUserIdFromToken(request);
            if (userId == null) {
                result.put("error", "Yêu cầu xác thực");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return result;
            }
            
            // Lấy thông tin sản phẩm từ form data
            String name = request.getParameter("name");
            String categoryIdStr = request.getParameter("category");
            String priceStr = request.getParameter("price");
            String description = request.getParameter("description");
            String specificationsJson = request.getParameter("specifications");
            
            // Kiểm tra dữ liệu đầu vào
            if (name == null || name.trim().isEmpty() ||
                categoryIdStr == null || categoryIdStr.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty()) {
                
                result.put("error", "Vui lòng điền đầy đủ thông tin sản phẩm");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Chuyển đổi dữ liệu
            int categoryId = Integer.parseInt(categoryIdStr);
            BigDecimal price = new BigDecimal(priceStr.replace(".", "").replace(",", "."));
            
            // Kiểm tra danh mục có tồn tại không
            if (!categoryDAO.isCategoryExists(categoryId)) {
                result.put("error", "Danh mục không tồn tại (ID: " + categoryId + ")");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Tạo đối tượng Product
            Product product = new Product();
            product.setName(name);
            product.setCategoryId(categoryId);
            product.setPrice(price);
            product.setDescription(description);
            product.setUserId(userId);
            
            // Xử lý thông số kỹ thuật
            List<ProductSpecification> specifications = new ArrayList<>();
            if (specificationsJson != null && !specificationsJson.trim().isEmpty()) {
                try {
                    JsonArray specsArray = JsonParser.parseString(specificationsJson).getAsJsonArray();
                    for (int i = 0; i < specsArray.size(); i++) {
                        JsonObject specObj = specsArray.get(i).getAsJsonObject();
                        String specName = specObj.get("name").getAsString();
                        String specValue = specObj.get("value").getAsString();
                        
                        ProductSpecification spec = new ProductSpecification(specName, specValue);
                        specifications.add(spec);
                    }
                } catch (Exception e) {
                    result.put("error", "Định dạng thông số kỹ thuật không hợp lệ");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return result;
                }
            }
            
            // Xử lý hình ảnh
            List<String> images = new ArrayList<>();
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if ((part.getName().equals("images") || part.getName().startsWith("images[")) && part.getSize() > 0) {
                    String imagePath = FileUploadUtil.saveFile(part, "products");
                    if (imagePath != null) {
                        images.add(imagePath);
                    }
                }
            }
            if (images.isEmpty()) {
                result.put("error", "Vui lòng tải lên ít nhất 1 hình ảnh sản phẩm");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            // Thêm sản phẩm vào cơ sở dữ liệu
            int productId = productService.addProduct(product, images, specifications);
            
            if (productId > 0) {
                result.put("id", productId);
                result.put("name", name);
                result.put("message", "Sản phẩm đã được thêm thành công");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                result.put("error", "Không thể thêm sản phẩm");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu thêm sản phẩm mới (JSON)
     * 
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> addProductJson(JsonObject jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra json request có tồn tại
            if (jsonRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng cung cấp dữ liệu sản phẩm");
                return result;
            }
            
            // Kiểm tra xác thực
            Integer userId = getUserIdFromToken(request);
            if (userId == null) {
                result.put("error", "Yêu cầu xác thực");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return result;
            }

            // Lấy thông tin sản phẩm từ JSON
            String name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            String categoryIdStr = jsonRequest.has("category") ? jsonRequest.get("category").getAsString() : null;
            String priceStr = jsonRequest.has("price") ? jsonRequest.get("price").getAsString() : null;
            String description = jsonRequest.has("description") ? jsonRequest.get("description").getAsString() : "";
            
            // Kiểm tra dữ liệu đầu vào
            if (name == null || name.trim().isEmpty() ||
                categoryIdStr == null || categoryIdStr.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty()) {
                
                result.put("error", "Vui lòng điền đầy đủ thông tin sản phẩm");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Chuyển đổi dữ liệu
            int categoryId = Integer.parseInt(categoryIdStr);
            BigDecimal price = new BigDecimal(priceStr.replace(".", "").replace(",", "."));
            
            // Kiểm tra danh mục có tồn tại không
            if (!categoryDAO.isCategoryExists(categoryId)) {
                result.put("error", "Danh mục không tồn tại (ID: " + categoryId + ")");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Tạo đối tượng Product
            Product product = new Product();
            product.setName(name);
            product.setCategoryId(categoryId);
            product.setPrice(price);
            product.setDescription(description);
            product.setUserId(userId);
            
            // Xử lý thông số kỹ thuật
            List<ProductSpecification> specifications = new ArrayList<>();
            if (jsonRequest.has("specifications") && jsonRequest.get("specifications").isJsonArray()) {
                JsonArray specsArray = jsonRequest.get("specifications").getAsJsonArray();
                for (int i = 0; i < specsArray.size(); i++) {
                    JsonObject specObj = specsArray.get(i).getAsJsonObject();
                    String specName = specObj.get("name").getAsString();
                    String specValue = specObj.get("value").getAsString();
                    
                    ProductSpecification spec = new ProductSpecification(specName, specValue);
                    specifications.add(spec);
                }
            }
            
            // Thêm sản phẩm vào cơ sở dữ liệu
            try {
                int productId = productService.addProduct(product, null, specifications);
                
                if (productId > 0) {
                    result.put("id", productId);
                    result.put("name", name);
                    result.put("message", "Sản phẩm đã được thêm thành công");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    result.put("error", "Không thể thêm sản phẩm");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
                e.printStackTrace();
                result.put("error", "Không thể thêm sản phẩm: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi chung: " + e.getMessage());
            e.printStackTrace();
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu cập nhật sản phẩm
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param productId ID sản phẩm cần cập nhật
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> updateProduct(HttpServletRequest request, HttpServletResponse response, int productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            Integer userId = getUserIdFromToken(request);
            if (userId == null) {
                result.put("error", "Yêu cầu xác thực");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return result;
            }
            
            // Kiểm tra sản phẩm tồn tại
            if (!productService.productExists(productId)) {
                result.put("error", "Không tìm thấy sản phẩm");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return result;
            }
            
            // Kiểm tra quyền chỉnh sửa
            if (!productService.canUserEditProduct(productId, userId)) {
                result.put("error", "Bạn không có quyền chỉnh sửa sản phẩm này");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return result;
            }
            
            // Lấy thông tin sản phẩm từ form data
            String name = request.getParameter("name");
            String categoryIdStr = request.getParameter("category");
            String priceStr = request.getParameter("price");
            String description = request.getParameter("description");
            String specificationsJson = request.getParameter("specifications");
            
            // Kiểm tra dữ liệu đầu vào
            if (name == null || name.trim().isEmpty() ||
                categoryIdStr == null || categoryIdStr.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty()) {
                
                result.put("error", "Vui lòng điền đầy đủ thông tin sản phẩm");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Chuyển đổi dữ liệu
            int categoryId = Integer.parseInt(categoryIdStr);
            BigDecimal price = new BigDecimal(priceStr.replace(".", "").replace(",", "."));
            
            // Kiểm tra danh mục có tồn tại không
            if (!categoryDAO.isCategoryExists(categoryId)) {
                result.put("error", "Danh mục không tồn tại (ID: " + categoryId + ")");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return result;
            }
            
            // Tạo đối tượng Product
            Product product = new Product();
            product.setId(productId);
            product.setName(name);
            product.setCategoryId(categoryId);
            product.setPrice(price);
            product.setDescription(description);
            product.setUserId(userId);
            
            // Xử lý thông số kỹ thuật
            List<ProductSpecification> specifications = new ArrayList<>();
            if (specificationsJson != null && !specificationsJson.trim().isEmpty()) {
                try {
                    JsonArray specsArray = JsonParser.parseString(specificationsJson).getAsJsonArray();
                    for (int i = 0; i < specsArray.size(); i++) {
                        JsonObject specObj = specsArray.get(i).getAsJsonObject();
                        String specName = specObj.get("name").getAsString();
                        String specValue = specObj.get("value").getAsString();
                        
                        ProductSpecification spec = new ProductSpecification(specName, specValue);
                        specifications.add(spec);
                    }
                } catch (Exception e) {
                    result.put("error", "Định dạng thông số kỹ thuật không hợp lệ");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return result;
                }
            }
            
            // Xử lý hình ảnh
            List<String> images = new ArrayList<>();
            boolean hasNewImages = false;
            
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (part.getName().startsWith("image_") && part.getSize() > 0) {
                    // Lưu file và lấy đường dẫn
                    String imagePath = FileUploadUtil.saveFile(part, "products");
                    if (imagePath != null) {
                        images.add(imagePath);
                        hasNewImages = true;
                    }
                }
            }
            
            // Cập nhật sản phẩm trong cơ sở dữ liệu
            boolean success = productService.updateProduct(
                product, 
                hasNewImages ? images : null, // Chỉ cập nhật hình ảnh nếu có hình mới
                !specifications.isEmpty() ? specifications : null // Chỉ cập nhật thông số nếu có thông số mới
            );
            
            if (success) {
                result.put("id", productId);
                result.put("name", name);
                result.put("message", "Sản phẩm đã được cập nhật thành công");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                result.put("error", "Không thể cập nhật sản phẩm");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xử lý yêu cầu xóa sản phẩm
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param productId ID sản phẩm cần xóa
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> deleteProduct(HttpServletRequest request, HttpServletResponse response, int productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            Integer userId = getUserIdFromToken(request);
            if (userId == null) {
                result.put("error", "Yêu cầu xác thực");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return result;
            }
            
            // Kiểm tra sản phẩm tồn tại
            if (!productService.productExists(productId)) {
                result.put("error", "Không tìm thấy sản phẩm");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return result;
            }
            
            // Kiểm tra quyền xóa
            if (!productService.canUserEditProduct(productId, userId)) {
                result.put("error", "Bạn không có quyền xóa sản phẩm này");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return result;
            }
            
            // Xóa sản phẩm
            boolean success = productService.deleteProduct(productId);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Sản phẩm đã được xóa thành công");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                result.put("error", "Không thể xóa sản phẩm");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Lấy tham số kiểu int từ request với giá trị mặc định
     * 
     * @param request Yêu cầu HTTP
     * @param paramName Tên tham số
     * @param defaultValue Giá trị mặc định
     * @return Giá trị tham số
     */
    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.trim().isEmpty()) {
            try {
                return Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return defaultValue;
    }
    
    /**
     * Lấy ID người dùng từ token JWT
     * 
     * @param request Yêu cầu HTTP
     * @return ID người dùng hoặc null nếu không có token hợp lệ
     */
    private Integer getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // Token không hợp lệ
                return null;
            }
        }
        return null;
    }
    
    /**
     * Định dạng giá tiền cho các sản phẩm
     * 
     * @param result Kết quả chứa danh sách sản phẩm
     */
    @SuppressWarnings("unchecked")
    private void formatProductPrices(Map<String, Object> result) {
        if (result.containsKey("products")) {
            List<Product> products = (List<Product>) result.get("products");
            List<Map<String, Object>> formattedProducts = new ArrayList<>();
            NumberFormat formatter = new DecimalFormat("#,###.##");
            
            for (Product product : products) {
                Map<String, Object> productMap = productToMap(product);
                formattedProducts.add(productMap);
            }
            
            result.put("products", formattedProducts);
        }
    }
    
    /**
     * Chuyển đổi đối tượng Product thành Map
     * 
     * @param product Đối tượng Product
     * @return Map chứa thông tin sản phẩm
     */
    private Map<String, Object> productToMap(Product product) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("category", product.getCategoryName());
        result.put("rating", product.getRating());
        result.put("reviewCount", product.getReviewCount());
        result.put("isReviewed", product.isReviewed());
        
        // Định dạng giá tiền
        NumberFormat formatter = new DecimalFormat("#,###.##");
        result.put("price", formatter.format(product.getPrice()) + " ₫");
        
        // Thêm hình ảnh
        result.put("images", product.getImages());
        
        // Thêm thông số kỹ thuật
        List<String> specs = new ArrayList<>();
        if (product.getSpecifications() != null) {
            for (ProductSpecification spec : product.getSpecifications()) {
                specs.add(spec.getName() + ": " + spec.getValue());
            }
        }
        result.put("specs", specs);
        
        // Thêm trạng thái bookmark
        result.put("isBookmarked", product.isBookmarked());
        
        return result;
    }
    /**
     * Đánh dấu bookmark vào sản phẩm
     */
    public Map<String, Object> addBookmark(int productId, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Starting addBookmark process...");
        Integer userId = getUserIdFromToken(request);
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            System.out.println("User ID not found. Authentication required.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("error", "Yêu cầu xác thực");
            return result;
        }
        Product product = productService.getProductById(productId, userId);
        if (product == null) {
            System.out.println("Product not found for ID: " + productId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result.put("error", "Không tìm thấy sản phẩm");
            return result;
        }
        int status = bookmarkDAO.toggleBookmark(productId, userId);
        System.out.println("Bookmark ID generated");

        if (status != -1) {
            System.out.println("Bookmark added successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
            result.put("message", status == 1 ?"Thêm bookmark thành công" : "Xoá bookmark thành công");
            result.put("status", status);
            response.setContentType("application/json");
            return result;
        }
        System.out.println("Failed to add bookmark for product ID: " + productId);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        result.put("error", "Không thể cập nhật bookmark");
        return result;
    }
    
    /**
     * Lấy danh sách sản phẩm đã bookmark của user
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getBookmarkedProducts(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Chưa đăng nhập");
                return result;
            }
            
            // Lấy token và userId
            String token = authHeader.substring(7);
            Integer userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Token không hợp lệ");
                return result;
            }
            
            // Lấy tham số phân trang
            int page = getIntParameter(request, "page", 1);
            int limit = getIntParameter(request, "limit", 10);
            String sort = request.getParameter("sort"); // có thể sort theo giá, tên, ngày bookmark
            
            // Lấy danh sách sản phẩm đã bookmark
            List<Map<String, Object>> products = productService.getBookmarkedProducts(userId, page, limit, sort);
            int totalProducts = productService.getBookmarkedProductCount(userId);
            
            // Tính toán thông tin phân trang
            int totalPages = (int) Math.ceil((double) totalProducts / limit);
            
            // Trả về kết quả
            result.put("products", products);
            result.put("pagination", Map.of(
                "currentPage", page,
                "totalPages", totalPages,
                "totalItems", totalProducts,
                "itemsPerPage", limit
            ));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
