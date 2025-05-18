package com.example.api.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.api.util.DatabaseUtil;

public class BookmarkDAO {
    /**
     * Kiểm tra xem bookmark đã tồn tại chưa
     * @param productId ID sản phẩm
     * @param userId ID người dùng
     * @return true nếu tồn tại, false nếu không
     */
    public boolean isBookmarkExists(int productId, int userId) {
        String sql = "SELECT id FROM bookmarks WHERE product_id = ? AND user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra bookmark: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm hoặc xóa bookmark tùy thuộc vào trạng thái hiện tại
     * @param productId ID sản phẩm
     * @param userId ID người dùng
     * @return ID bookmark nếu thêm thành công, 0 nếu xóa thành công, -1 nếu thất bại
     */
    public int toggleBookmark(int productId, int userId) {
        if (isBookmarkExists(productId, userId)) {
            // Nếu đã tồn tại thì xóa bookmark
            int status = deleteBookmark(productId, userId);
            return status == 0 ? 2 : -1;
        } else {
            // Nếu chưa tồn tại thì thêm mới
            int status = addBookmark(productId, userId);
            return status > 0 ? 1 : -1;
        }
    }

    private int addBookmark(int productId, int userId) {
        String sql = "INSERT INTO bookmarks (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm bookmark: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    private int deleteBookmark(int productId, int userId) {
        String sql = "DELETE FROM bookmarks WHERE product_id = ? AND user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? 0 : -1;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa bookmark: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
}
