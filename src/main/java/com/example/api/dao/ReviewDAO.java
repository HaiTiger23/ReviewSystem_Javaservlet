package com.example.api.dao;

import com.example.api.model.Review;
import com.example.api.model.User;
import com.example.api.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác với bảng reviews trong cơ sở dữ liệu
 */
public class ReviewDAO {
    private static final Logger LOGGER = Logger.getLogger(ReviewDAO.class.getName());
    
    /**
     * Lấy danh sách đánh giá của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @param page Số trang
     * @param limit Số lượng đánh giá mỗi trang
     * @param sort Sắp xếp (date_desc, rating_desc, helpful_desc)
     * @param userId ID người dùng hiện tại (nếu đã đăng nhập)
     * @return Map chứa danh sách đánh giá và thông tin phân trang
     */
    public Map<String, Object> getProductReviews(int productId, int page, int limit, String sort, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Đếm tổng số đánh giá
            String countQuery = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
            try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                countStmt.setInt(1, productId);
                ResultSet countRs = countStmt.executeQuery();
                int total = 0;
                if (countRs.next()) {
                    total = countRs.getInt(1);
                }
                
                // Tính toán phân trang
                int offset = (page - 1) * limit;
                int totalPages = (int) Math.ceil((double) total / limit);
                
                // Tạo thông tin phân trang
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("total", total);
                pagination.put("page", page);
                pagination.put("limit", limit);
                pagination.put("totalPages", totalPages);
                result.put("pagination", pagination);
                
                // Nếu không có đánh giá, trả về danh sách rỗng
                if (total == 0) {
                    result.put("reviews", reviews);
                    return result;
                }
            }
            
            // Xác định cách sắp xếp
            String orderBy;
            switch (sort) {
                case "rating_desc":
                    orderBy = "r.rating DESC";
                    break;
                case "helpful_desc":
                    orderBy = "r.helpful_count DESC";
                    break;
                case "date_desc":
                default:
                    orderBy = "r.created_at DESC";
                    break;
            }
            
            // Truy vấn lấy đánh giá với thông tin người dùng
            String query = "SELECT r.*, u.id as user_id, u.name as user_name, u.avatar as user_avatar " +
                           "FROM reviews r " +
                           "JOIN users u ON r.user_id = u.id " +
                           "WHERE r.product_id = ? " +
                           "ORDER BY " + orderBy + " " +
                           "LIMIT ? OFFSET ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, productId);
                stmt.setInt(2, limit);
                stmt.setInt(3, (page - 1) * limit);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getInt("id"));
                    review.setProductId(rs.getInt("product_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setContent(rs.getString("content"));
                    review.setHelpfulCount(rs.getInt("helpful_count"));
                    review.setCreatedAt(rs.getTimestamp("created_at"));
                    review.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // Thêm thông tin người dùng
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("user_name"));
                    user.setAvatar(rs.getString("user_avatar"));
                    review.setUser(user);
                    
                    // Kiểm tra người dùng hiện tại đã đánh dấu đánh giá này là hữu ích chưa
                    if (userId != null) {
                        String helpfulQuery = "SELECT is_helpful FROM review_helpful WHERE review_id = ? AND user_id = ?";
                        try (PreparedStatement helpfulStmt = conn.prepareStatement(helpfulQuery)) {
                            helpfulStmt.setInt(1, review.getId());
                            helpfulStmt.setInt(2, userId);
                            ResultSet helpfulRs = helpfulStmt.executeQuery();
                            if (helpfulRs.next()) {
                                review.setIsHelpful(helpfulRs.getBoolean("is_helpful"));
                            } else {
                                review.setIsHelpful(false);
                            }
                        }
                    } else {
                        review.setIsHelpful(false);
                    }
                    
                    reviews.add(review);
                }
            }
            
            result.put("reviews", reviews);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách đánh giá: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * Thêm đánh giá mới
     * 
     * @param review Đối tượng đánh giá
     * @return ID của đánh giá mới, hoặc -1 nếu có lỗi
     */
    public int addReview(Review review) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
            String checkQuery = "SELECT id FROM reviews WHERE product_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, review.getProductId());
                checkStmt.setInt(2, review.getUserId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Người dùng đã đánh giá sản phẩm này
                    return -2;
                }
            }
            
            // Thêm đánh giá mới
            String query = "INSERT INTO reviews (product_id, user_id, rating, content, helpful_count) VALUES (?, ?, ?, ?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, review.getProductId());
                stmt.setInt(2, review.getUserId());
                stmt.setInt(3, review.getRating());
                stmt.setString(4, review.getContent());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    return -1;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int reviewId = generatedKeys.getInt(1);
                        
                        // Cập nhật rating trung bình của sản phẩm
                        updateProductRating(conn, review.getProductId());
                        
                        return reviewId;
                    } else {
                        return -1;
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm đánh giá: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Cập nhật đánh giá
     * 
     * @param reviewId ID đánh giá
     * @param rating Điểm đánh giá
     * @param content Nội dung đánh giá
     * @param userId ID người dùng (để kiểm tra quyền)
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public boolean updateReview(int reviewId, int rating, String content, int userId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Kiểm tra quyền cập nhật
            String checkQuery = "SELECT product_id FROM reviews WHERE id = ? AND user_id = ?";
            int productId;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, reviewId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Không tìm thấy đánh giá hoặc không có quyền
                    return false;
                }
                productId = rs.getInt("product_id");
            }
            
            // Cập nhật đánh giá
            String query = "UPDATE reviews SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, rating);
                stmt.setString(2, content);
                stmt.setInt(3, reviewId);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Cập nhật rating trung bình của sản phẩm
                    updateProductRating(conn, productId);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật đánh giá: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Xóa đánh giá
     * 
     * @param reviewId ID đánh giá
     * @param userId ID người dùng (để kiểm tra quyền)
     * @return true nếu xóa thành công, false nếu có lỗi
     */
    public boolean deleteReview(int reviewId, int userId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Kiểm tra quyền xóa
            String checkQuery = "SELECT product_id FROM reviews WHERE id = ? AND user_id = ?";
            int productId;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, reviewId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Không tìm thấy đánh giá hoặc không có quyền
                    return false;
                }
                productId = rs.getInt("product_id");
            }
            
            // Xóa đánh giá
            String query = "DELETE FROM reviews WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reviewId);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Cập nhật rating trung bình của sản phẩm
                    updateProductRating(conn, productId);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa đánh giá: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Đánh dấu đánh giá là hữu ích
     * 
     * @param reviewId ID đánh giá
     * @param userId ID người dùng
     * @param isHelpful Có hữu ích hay không
     * @return Số lượng đánh dấu hữu ích mới, hoặc -1 nếu có lỗi
     */
    public int markReviewHelpful(int reviewId, int userId, boolean isHelpful) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Kiểm tra xem đánh giá có tồn tại không
                String checkReviewQuery = "SELECT id FROM reviews WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkReviewQuery)) {
                    checkStmt.setInt(1, reviewId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next()) {
                        // Đánh giá không tồn tại
                        return -1;
                    }
                }
                
                // Kiểm tra xem người dùng đã đánh dấu đánh giá này chưa
                String checkQuery = "SELECT id, is_helpful FROM review_helpful WHERE review_id = ? AND user_id = ?";
                boolean exists = false;
                boolean currentValue = false;
                int helpfulId = 0;
                
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, reviewId);
                    checkStmt.setInt(2, userId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        exists = true;
                        helpfulId = rs.getInt("id");
                        currentValue = rs.getBoolean("is_helpful");
                    }
                }
                
                if (exists) {
                    // Nếu giá trị mới giống giá trị cũ, không cần cập nhật
                    if (currentValue == isHelpful) {
                        // Lấy số lượng đánh dấu hữu ích hiện tại
                        String countQuery = "SELECT helpful_count FROM reviews WHERE id = ?";
                        try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                            countStmt.setInt(1, reviewId);
                            ResultSet countRs = countStmt.executeQuery();
                            if (countRs.next()) {
                                conn.commit();
                                return countRs.getInt("helpful_count");
                            }
                        }
                    }
                    
                    // Cập nhật đánh dấu
                    String updateQuery = "UPDATE review_helpful SET is_helpful = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setBoolean(1, isHelpful);
                        updateStmt.setInt(2, helpfulId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Thêm đánh dấu mới
                    String insertQuery = "INSERT INTO review_helpful (review_id, user_id, is_helpful) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, reviewId);
                        insertStmt.setInt(2, userId);
                        insertStmt.setBoolean(3, isHelpful);
                        insertStmt.executeUpdate();
                    }
                }
                
                // Cập nhật số lượng đánh dấu hữu ích trong bảng reviews
                String updateHelpfulCountQuery = "UPDATE reviews SET helpful_count = " +
                                                "(SELECT COUNT(*) FROM review_helpful WHERE review_id = ? AND is_helpful = true) " +
                                                "WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateHelpfulCountQuery)) {
                    updateStmt.setInt(1, reviewId);
                    updateStmt.setInt(2, reviewId);
                    updateStmt.executeUpdate();
                }
                
                // Lấy số lượng đánh dấu hữu ích mới
                String countQuery = "SELECT helpful_count FROM reviews WHERE id = ?";
                try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                    countStmt.setInt(1, reviewId);
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        int helpfulCount = countRs.getInt("helpful_count");
                        conn.commit();
                        return helpfulCount;
                    }
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đánh dấu đánh giá là hữu ích: " + e.getMessage(), e);
        }
        
        return -1;
    }
    
    /**
     * Lấy thông tin đánh giá theo ID
     * 
     * @param reviewId ID đánh giá
     * @return Đối tượng đánh giá, hoặc null nếu không tìm thấy
     */
    public Review getReviewById(int reviewId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT r.*, u.name as user_name, u.avatar as user_avatar " +
                           "FROM reviews r " +
                           "JOIN users u ON r.user_id = u.id " +
                           "WHERE r.id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reviewId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getInt("id"));
                    review.setProductId(rs.getInt("product_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setContent(rs.getString("content"));
                    review.setHelpfulCount(rs.getInt("helpful_count"));
                    review.setCreatedAt(rs.getTimestamp("created_at"));
                    review.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // Thêm thông tin người dùng
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("user_name"));
                    user.setAvatar(rs.getString("user_avatar"));
                    review.setUser(user);
                    
                    return review;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thông tin đánh giá: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Cập nhật điểm đánh giá trung bình của sản phẩm
     * 
     * @param conn Kết nối cơ sở dữ liệu
     * @param productId ID sản phẩm
     * @throws SQLException Nếu có lỗi SQL
     */
    private void updateProductRating(Connection conn, int productId) throws SQLException {
        String query = "UPDATE products p SET " +
                       "p.rating = (SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = ?), " +
                       "p.review_count = (SELECT COUNT(*) FROM reviews r WHERE r.product_id = ?) " +
                       "WHERE p.id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, productId);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
        }
    }
}
