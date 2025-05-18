package com.example.api.dao;

import com.example.api.model.Product;
import com.example.api.model.ProductImage;
import com.example.api.model.ProductSpecification;
import com.example.api.util.DatabaseUtil;
import com.example.api.util.SlugUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp Data Access Object cho Product, cung cấp các phương thức để tương tác với
 * cơ sở dữ liệu
 */
public class ProductDAO {

    /**
     * Lấy danh sách sản phẩm với phân trang và lọc
     * 
     * @param page       Số trang
     * @param limit      Số lượng sản phẩm trên mỗi trang
     * @param categoryId ID danh mục (nếu có)
     * @param search     Từ khóa tìm kiếm (nếu có)
     * @param sort       Cách sắp xếp (nếu có)
     * @return Danh sách sản phẩm
     */
    public Map<String, Object> getProducts(int page, int limit, Integer categoryId, String search, String sort) {
        Map<String, Object> result = new HashMap<>();
        List<Product> products = new ArrayList<>();

        // Tính toán offset cho phân trang
        int offset = (page - 1) * limit;

        // Xây dựng câu truy vấn SQL cơ bản
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT p.*, c.name as category_name, pi.image_path as primary_image " +
                        "FROM products p " +
                        "LEFT JOIN categories c ON p.category_id = c.id " +
                        "LEFT JOIN product_images pi ON p.id = pi.product_id AND pi.is_primary = TRUE ");

        // Thêm điều kiện WHERE nếu cần
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        if (categoryId != null) {
            sqlBuilder.append(hasWhere ? " AND " : " WHERE ");
            sqlBuilder.append("p.category_id = ?");
            params.add(categoryId);
            hasWhere = true;
        }
        if (search != null && !search.trim().isEmpty()) {
            System.out.println("search product: " + search);
            sqlBuilder.append(hasWhere ? " AND " : " WHERE ");
            sqlBuilder.append("(p.name LIKE ? OR p.description LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            hasWhere = true;
        }
        sqlBuilder.append(" GROUP BY p.id, c.name, pi.image_path");
        // Thêm ORDER BY
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    sqlBuilder.append(" ORDER BY p.price ASC");
                    break;
                case "price_desc":
                    sqlBuilder.append(" ORDER BY p.price DESC");
                    break;
                case "rating_desc":
                    sqlBuilder.append(" ORDER BY p.rating DESC, p.review_count DESC");
                    break;
                case "newest":
                    sqlBuilder.append(" ORDER BY p.created_at DESC");
                    break;
                default:
                    sqlBuilder.append(" ORDER BY p.id DESC");
                    break;
            }
        } else {
            sqlBuilder.append(" ORDER BY p.id DESC");
        }

        // Thêm LIMIT và OFFSET cho phân trang
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        // Truy vấn tổng số sản phẩm (không có LIMIT và OFFSET)
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) as total FROM products p");
        if (hasWhere) {
            countSqlBuilder.append(" WHERE ")
                    .append(sqlBuilder.substring(sqlBuilder.indexOf(" WHERE ") + 7, sqlBuilder.indexOf(" GROUP BY ")));
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Truy vấn tổng số sản phẩm
            int total = 0;
            try (PreparedStatement countStmt = conn.prepareStatement(countSqlBuilder.toString())) {
                // Thiết lập tham số cho câu truy vấn đếm
                for (int i = 0; i < params.size() - 2; i++) {
                    countStmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet countRs = countStmt.executeQuery()) {
                    if (countRs.next()) {
                        total = countRs.getInt("total");
                    }
                }
            }

            // Truy vấn danh sách sản phẩm
            try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
                // Thiết lập tham số cho câu truy vấn chính
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Product product = mapResultSetToProduct(rs);
                        product.setCategoryName(rs.getString("category_name"));

                        // Thêm đường dẫn ảnh chính nếu có
                        String primaryImage = rs.getString("primary_image");
                        if (primaryImage != null) {
                            List<String> images = new ArrayList<>();
                            images.add(primaryImage);
                            product.setImages(images);
                        }

                        products.add(product);
                    }
                }
            }

            // Tạo thông tin phân trang
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", total);
            pagination.put("page", page);
            pagination.put("limit", limit);
            pagination.put("totalPages", (int) Math.ceil((double) total / limit));

            // Thêm vào kết quả
            result.put("products", products);
            result.put("pagination", pagination);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     * 
     * @param productId ID sản phẩm
     * @param userId    ID người dùng (nếu đã đăng nhập)
     * @return Thông tin sản phẩm hoặc null nếu không tìm thấy
     */
    public Product getProductById(int productId, Integer userId) {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    product.setCategoryName(rs.getString("category_name"));

                    // Lấy danh sách hình ảnh
                    product.setImages(getProductImages(conn, productId));

                    // Lấy danh sách thông số kỹ thuật
                    product.setSpecifications(getProductSpecifications(conn, productId));

                    // Kiểm tra xem sản phẩm có được bookmark bởi người dùng không
                    if (userId != null) {
                        product.setBookmarked(isProductBookmarked(conn, productId, userId));
                    }

                    // Kiểm tra xem người dùng đã đánh giá chưa
                    if (userId != null) {
                        product.setReviewed(isProductReviewed(conn, productId, userId));
                        System.out.println("Reviewed: " + product.isReviewed());
                    }

                    return product;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lấy thông tin chi tiết sản phẩm theo slug
     * 
     * @param slug   Slug sản phẩm
     * @param userId ID người dùng (nếu đã đăng nhập)
     * @return Thông tin sản phẩm hoặc null nếu không tìm thấy
     */
    public Product getProductBySlug(String slug, Integer userId) {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.slug = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, slug);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    product.setCategoryName(rs.getString("category_name"));

                    // Lấy danh sách hình ảnh
                    product.setImages(getProductImages(conn, product.getId()));

                    // Lấy danh sách thông số kỹ thuật
                    product.setSpecifications(getProductSpecifications(conn, product.getId()));

                    // Kiểm tra xem sản phẩm có được bookmark bởi người dùng không
                    if (userId != null) {
                        product.setBookmarked(isProductBookmarked(conn, product.getId(), userId));
                    }

                    return product;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Thêm sản phẩm mới
     * 
     * @param product        Thông tin sản phẩm
     * @param images         Danh sách đường dẫn hình ảnh
     * @param specifications Danh sách thông số kỹ thuật
     * @return ID sản phẩm nếu thành công, -1 nếu thất bại
     */
    public int addProduct(Product product, List<String> images, List<ProductSpecification> specifications) {
        String sql = "INSERT INTO products (name, slug, description, price, category_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Kết nối thành công!");
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Tạo slug từ tên sản phẩm
                String slug = SlugUtil.createSlug(product.getName()) + "-" + System.currentTimeMillis();

                stmt.setString(1, product.getName());
                stmt.setString(2, slug);
                stmt.setString(3, product.getDescription());
                stmt.setBigDecimal(4, product.getPrice());
                stmt.setInt(5, product.getCategoryId());
                stmt.setInt(6, product.getUserId());

                System.out.println("SQL: " + sql);
                System.out.println("Name: " + product.getName());
                System.out.println("Slug: " + slug);
                System.out.println("Description: " + product.getDescription());
                System.out.println("Price: " + product.getPrice());
                System.out.println("CategoryId: " + product.getCategoryId());
                System.out.println("UserId: " + product.getUserId());

                int rowsAffected = stmt.executeUpdate();
                System.out.println("Rows affected: " + rowsAffected);

                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int productId = generatedKeys.getInt(1);
                            System.out.println("Generated product ID: " + productId);

                            // Thêm hình ảnh
                            if (images != null && !images.isEmpty()) {
                                addProductImages(conn, productId, images);
                            }
                            System.out.println("Adding product specifications" + specifications.size());
                            // Thêm thông số kỹ thuật
                            if (specifications != null && !specifications.isEmpty()) {
                                System.out.println("Adding product specifications...");

                                addProductSpecifications(conn, productId, specifications);
                            }

                            conn.commit(); // Commit transaction
                            System.out.println("Transaction committed successfully!");
                            return productId;
                        } else {
                            System.out.println("Không thể lấy ID sản phẩm đã tạo");
                        }
                    }
                } else {
                    System.out.println("Không thể thêm sản phẩm, rowsAffected = " + rowsAffected);
                }

                conn.rollback(); // Rollback nếu có lỗi
                System.out.println("Transaction rolled back!");
            } catch (SQLException e) {
                conn.rollback(); // Rollback nếu có lỗi
                System.err.println("SQL Exception: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                conn.setAutoCommit(true); // Khôi phục auto-commit
                System.out.println("Auto-commit restored!");
            }

        } catch (SQLException e) {
            System.err.println("Connection Exception: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning -1 (failure)");
        return -1;
    }

    /**
     * Cập nhật thông tin sản phẩm
     * 
     * @param product        Thông tin sản phẩm cần cập nhật
     * @param images         Danh sách đường dẫn hình ảnh mới (nếu có)
     * @param specifications Danh sách thông số kỹ thuật mới (nếu có)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateProduct(Product product, List<String> images, List<ProductSpecification> specifications) {
        String sql = "UPDATE products SET name = ?, slug = ?, description = ?, price = ?, category_id = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Tạo slug từ tên sản phẩm
                String slug = SlugUtil.createSlug(product.getName());

                stmt.setString(1, product.getName());
                stmt.setString(2, slug);
                stmt.setString(3, product.getDescription());
                stmt.setBigDecimal(4, product.getPrice());
                stmt.setInt(5, product.getCategoryId());
                stmt.setInt(6, product.getId());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Cập nhật hình ảnh nếu có
                    if (images != null && !images.isEmpty()) {
                        // Xóa hình ảnh cũ
                        deleteProductImages(conn, product.getId());
                        // Thêm hình ảnh mới
                        addProductImages(conn, product.getId(), images);
                    }

                    // Cập nhật thông số kỹ thuật nếu có
                    if (specifications != null && !specifications.isEmpty()) {
                        // Xóa thông số cũ
                        deleteProductSpecifications(conn, product.getId());
                        // Thêm thông số mới
                        addProductSpecifications(conn, product.getId(), specifications);
                    }

                    conn.commit(); // Commit transaction
                    return true;
                }

                conn.rollback(); // Rollback nếu có lỗi
            } catch (SQLException e) {
                conn.rollback(); // Rollback nếu có lỗi
                throw e;
            } finally {
                conn.setAutoCommit(true); // Khôi phục auto-commit
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa sản phẩm
     * 
     * @param productId ID sản phẩm cần xóa
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra sản phẩm có tồn tại không
     * 
     * @param productId ID sản phẩm
     * @return true nếu tồn tại, false nếu không
     */
    public boolean productExists(int productId) {
        String sql = "SELECT id FROM products WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra người dùng có quyền chỉnh sửa sản phẩm không
     * 
     * @param productId ID sản phẩm
     * @param userId    ID người dùng
     * @return true nếu có quyền, false nếu không
     */
    public boolean canUserEditProduct(int productId, int userId) {
        String sql = "SELECT id FROM products WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách hình ảnh của sản phẩm
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     * @return Danh sách đường dẫn hình ảnh
     */
    private List<String> getProductImages(Connection conn, int productId) throws SQLException {
        List<String> images = new ArrayList<>();

        String sql = "SELECT image_path FROM product_images WHERE product_id = ? ORDER BY is_primary DESC, sort_order ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(rs.getString("image_path"));
                }
            }
        }

        return images;
    }

    /**
     * Lấy danh sách thông số kỹ thuật của sản phẩm
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     * @return Danh sách thông số kỹ thuật
     */
    private List<ProductSpecification> getProductSpecifications(Connection conn, int productId) throws SQLException {
        List<ProductSpecification> specifications = new ArrayList<>();

        String sql = "SELECT id, name, value FROM product_specifications WHERE product_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductSpecification spec = new ProductSpecification();
                    spec.setId(rs.getInt("id"));
                    spec.setProductId(productId);
                    spec.setName(rs.getString("name"));
                    spec.setValue(rs.getString("value"));
                    specifications.add(spec);
                }
            }
        }

        return specifications;
    }

    /**
     * Kiểm tra sản phẩm có được bookmark bởi người dùng không
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     * @param userId    ID người dùng
     * @return true nếu đã bookmark, false nếu chưa
     */
    private boolean isProductBookmarked(Connection conn, int productId, int userId) throws SQLException {
        String sql = "SELECT id FROM bookmarks WHERE product_id = ? AND user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Kiểm tra người dùng đã đánh giá sản phẩm chưa
     *
     * @param conn      Kết nối DB
     * @param productId ID sản phẩm
     * @param userId    ID người dùng
     * @return true nếu đã đánh giá, false nếu chưa
     */
    private boolean isProductReviewed(Connection conn, int productId, int userId) throws SQLException {
        String sql = "SELECT id FROM reviews WHERE product_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm hình ảnh cho sản phẩm
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     * @param images    Danh sách đường dẫn hình ảnh
     */
    private void addProductImages(Connection conn, int productId, List<String> images) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_path, is_primary, sort_order) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < images.size(); i++) {
                stmt.setInt(1, productId);
                stmt.setString(2, images.get(i));
                stmt.setBoolean(3, i == 0); // Ảnh đầu tiên là ảnh chính
                stmt.setInt(4, i);
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    /**
     * Thêm thông số kỹ thuật cho sản phẩm
     * 
     * @param conn           Kết nối đến cơ sở dữ liệu
     * @param productId      ID sản phẩm
     * @param specifications Danh sách thông số kỹ thuật
     */
    private void addProductSpecifications(Connection conn, int productId, List<ProductSpecification> specifications)
            throws SQLException {
        String sql = "INSERT INTO product_specifications (product_id, name, value) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ProductSpecification spec : specifications) {

                System.out.println("Adding product specification: " + spec.getName() + " - " + spec.getValue());
                stmt.setInt(1, productId);
                stmt.setString(2, spec.getName());
                stmt.setString(3, spec.getValue());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    /**
     * Xóa hình ảnh của sản phẩm
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     */
    private void deleteProductImages(Connection conn, int productId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE product_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa thông số kỹ thuật của sản phẩm
     * 
     * @param conn      Kết nối đến cơ sở dữ liệu
     * @param productId ID sản phẩm
     */
    private void deleteProductSpecifications(Connection conn, int productId) throws SQLException {
        String sql = "DELETE FROM product_specifications WHERE product_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    /**
     * Chuyển đổi ResultSet thành đối tượng Product
     * 
     * @param rs ResultSet từ truy vấn
     * @return Đối tượng Product
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setSlug(rs.getString("slug"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setUserId(rs.getInt("user_id"));
        product.setRating(rs.getDouble("rating"));
        product.setReviewCount(rs.getInt("review_count"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
}
