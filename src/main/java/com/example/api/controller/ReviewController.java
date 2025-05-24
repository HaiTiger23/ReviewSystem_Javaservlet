package com.example.api.controller;

import com.example.api.dao.ReviewDAO;
import com.example.api.model.Review;
import com.example.api.model.User;
import com.example.api.service.AuthService;
import com.example.api.util.JwtUtil;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý các yêu cầu liên quan đến đánh giá sản phẩm
 */
public class ReviewController {
    private ReviewDAO reviewDAO;
    private AuthService authService;
    
    public ReviewController() {
        this.reviewDAO = new ReviewDAO();
        this.authService = new AuthService();
    }
    
    /**
     * Lấy danh sách đánh giá của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @param page Số trang
     * @param limit Số lượng mỗi trang
     * @param sort Cách sắp xếp
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getProductReviews(int productId, int page, int limit, String sort, 
                                                HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra xác thực
            Integer userId = null;
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    userId = JwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // Token không hợp lệ
                }
            }
            
            // Lấy danh sách đánh giá
            result = reviewDAO.getProductReviews(productId, page, limit, sort, userId);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Thêm đánh giá mới
     * 
     * @param productId ID sản phẩm
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> addReview(int productId, JsonObject jsonRequest, 
                                        HttpServletRequest request, HttpServletResponse response) {
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
                result.put("error", "Vui lòng cung cấp dữ liệu đánh giá");
                return result;
            }
            
            Integer rating = jsonRequest.has("rating") ? jsonRequest.get("rating").getAsInt() : null;
            String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : null;
            
            if (rating == null || content == null || content.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng điền đầy đủ thông tin đánh giá");
                return result;
            }
            
            if (rating < 1 || rating > 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Điểm đánh giá phải từ 1 đến 5");
                return result;
            }
            
            // Tạo đối tượng đánh giá
            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(userId);
            review.setRating(rating);
            review.setContent(content);
            
            // Thêm đánh giá
            int reviewId = reviewDAO.addReview(review);
            
            if (reviewId > 0) {
                // Thành công
                response.setStatus(HttpServletResponse.SC_CREATED);
                result.put("id", reviewId);
                result.put("rating", rating);
                result.put("content", content);
                
                // Format ngày tháng
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = dateFormat.format(new java.util.Date());
                result.put("date", formattedDate);
                
                result.put("message", "Đánh giá của bạn đã được gửi thành công");
            } else if (reviewId == -2) {
                // Người dùng đã đánh giá sản phẩm này trước đó
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                result.put("error", "Bạn đã đánh giá sản phẩm này trước đó");
            } else {
                // Lỗi khi thêm đánh giá
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("error", "Không thể thêm đánh giá");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Cập nhật đánh giá
     * 
     * @param reviewId ID đánh giá
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> updateReview(int reviewId, JsonObject jsonRequest, 
                                           HttpServletRequest request, HttpServletResponse response) {
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
                result.put("error", "Vui lòng cung cấp dữ liệu đánh giá");
                return result;
            }
            
            Integer rating = jsonRequest.has("rating") ? jsonRequest.get("rating").getAsInt() : null;
            String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : null;
            
            if (rating == null || content == null || content.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng điền đầy đủ thông tin đánh giá");
                return result;
            }
            
            if (rating < 1 || rating > 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Điểm đánh giá phải từ 1 đến 5");
                return result;
            }
            
            // Kiểm tra đánh giá có tồn tại không
            Review existingReview = reviewDAO.getReviewById(reviewId);
            if (existingReview == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("error", "Không tìm thấy đánh giá");
                return result;
            }
            
            // Cập nhật đánh giá
            boolean success = reviewDAO.updateReview(reviewId, rating, content, userId);
            
            if (success) {
                // Thành công
                response.setStatus(HttpServletResponse.SC_OK);
                result.put("id", reviewId);
                result.put("message", "Cập nhật đánh giá thành công");
            } else {
                // Không có quyền cập nhật đánh giá
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Không có quyền cập nhật đánh giá");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Xóa đánh giá
     * 
     * @param reviewId ID đánh giá
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> deleteReview(int reviewId, HttpServletRequest request, HttpServletResponse response) {
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
            
            // Kiểm tra đánh giá có tồn tại không
            Review existingReview = reviewDAO.getReviewById(reviewId);
            if (existingReview == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("error", "Không tìm thấy đánh giá");
                return result;
            }
            User user = authService.getUserFromRequest(request);
            System.out.println(String.format("User ID: %d, Review User ID: %d, Is Admin: %s", user.getId(), existingReview.getUserId(), user.isAdmin() ? "true" : "false"));
            // đánh giá không phải của bạn hoặc bạn không phải admin
            if ((existingReview.getUserId() != user.getId()) && !user.isAdmin()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                System.out.println("You do not have permission to delete this review");
                result.put("error", "Bạn không có quyền xóa đánh giá này");
                return result;
            }
            
            // Xóa đánh giá
            boolean success = reviewDAO.deleteReview(reviewId, userId);
            
            if (success) {
                // Thành công
                response.setStatus(HttpServletResponse.SC_OK);
                result.put("message", "Xóa đánh giá thành công");
            } else {
                // Không có quyền xóa đánh giá
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("error", "Không có quyền xóa đánh giá");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Đánh dấu đánh giá là hữu ích
     * 
     * @param reviewId ID đánh giá
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> markReviewHelpful(int reviewId, JsonObject jsonRequest, 
                                               HttpServletRequest request, HttpServletResponse response) {
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
                result.put("error", "Vui lòng cung cấp dữ liệu");
                return result;
            }
            
            boolean isHelpful = jsonRequest.has("isHelpful") ? jsonRequest.get("isHelpful").getAsBoolean() : true;
            
            // Đánh dấu đánh giá là hữu ích
            int helpfulCount = reviewDAO.markReviewHelpful(reviewId, userId, isHelpful);
            
            if (helpfulCount >= 0) {
                // Thành công
                response.setStatus(HttpServletResponse.SC_OK);
                result.put("id", reviewId);
                result.put("helpfulCount", helpfulCount);
                result.put("isHelpful", isHelpful);
            } else {
                // Không tìm thấy đánh giá
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("error", "Không tìm thấy đánh giá");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Lấy danh sách đánh giá của người dùng đã đăng nhập
     * 
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> getUserReviews(HttpServletRequest request, HttpServletResponse response) {
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
            
            // Lấy tham số phân trang từ request
            int page = 1;
            int limit = 10;
            String sort = "date_desc";
            
            String pageParam = request.getParameter("page");
            String limitParam = request.getParameter("limit");
            String sortParam = request.getParameter("sort");
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // Giữ giá trị mặc định
                }
            }
            
            if (limitParam != null && !limitParam.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit < 1) limit = 10;
                    if (limit > 50) limit = 50;
                } catch (NumberFormatException e) {
                    // Giữ giá trị mặc định
                }
            }
            
            if (sortParam != null && !sortParam.trim().isEmpty()) {
                if (sortParam.equals("rating_desc") || sortParam.equals("helpful_desc")) {
                    sort = sortParam;
                }
            }
            
            // Lấy danh sách đánh giá của người dùng
            result = reviewDAO.getUserReviews(userId, page, limit, sort);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Cập nhật đánh giá của người dùng
     * 
     * @param reviewId ID đánh giá
     * @param jsonRequest Dữ liệu JSON từ request body
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return Kết quả xử lý dạng JSON
     */
    public Map<String, Object> updateUserReview(int reviewId, JsonObject jsonRequest, 
                                              HttpServletRequest request, HttpServletResponse response) {
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
                result.put("error", "Vui lòng cung cấp dữ liệu đánh giá");
                return result;
            }
            
            Integer rating = jsonRequest.has("rating") ? jsonRequest.get("rating").getAsInt() : null;
            String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : null;
            
            if (rating == null || content == null || content.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Vui lòng điền đầy đủ thông tin đánh giá");
                return result;
            }
            
            if (rating < 1 || rating > 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("error", "Điểm đánh giá phải từ 1 đến 5");
                return result;
            }
            
            // Kiểm tra đánh giá có tồn tại không và có phải của user không
            Review existingReview = reviewDAO.getReviewById(reviewId);
            if (existingReview == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("error", "Không tìm thấy đánh giá");
                return result;
            }
            
            if (existingReview.getUserId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("error", "Bạn không có quyền cập nhật đánh giá này");
                return result;
            }
            
            // Cập nhật đánh giá
            boolean success = reviewDAO.updateReview(reviewId, rating, content, userId);
            
            if (success) {
                // Lấy thông tin đánh giá sau khi cập nhật
                Review updatedReview = reviewDAO.getReviewById(reviewId);
                
                // Thành công
                response.setStatus(HttpServletResponse.SC_OK);
                result.put("message", "Cập nhật đánh giá thành công");
                result.put("review", updatedReview);
            } else {
                // Lỗi khi cập nhật
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("error", "Không thể cập nhật đánh giá");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
