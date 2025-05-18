package com.example.api.service;

import com.example.api.dao.ProductDAO;
import com.example.api.model.Product;
import com.example.api.model.ProductSpecification;

import java.util.List;
import java.util.Map;

/**
 * Lớp dịch vụ xử lý các chức năng liên quan đến sản phẩm
 */
public class ProductService {
    private ProductDAO productDAO;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Lấy danh sách sản phẩm với phân trang và lọc
     * 
     * @param page Số trang
     * @param limit Số lượng sản phẩm trên mỗi trang
     * @param categoryId ID danh mục (nếu có)
     * @param search Từ khóa tìm kiếm (nếu có)
     * @param sort Cách sắp xếp (nếu có)
     * @return Danh sách sản phẩm và thông tin phân trang
     */
    public Map<String, Object> getProducts(int page, int limit, Integer categoryId, String search, String sort) {
        return productDAO.getProducts(page, limit, categoryId, search, sort);
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     * 
     * @param productId ID sản phẩm
     * @param userId ID người dùng (nếu đã đăng nhập)
     * @return Thông tin sản phẩm hoặc null nếu không tìm thấy
     */
    public Product getProductById(int productId, Integer userId) {
        return productDAO.getProductById(productId, userId);
    }
    
    /**
     * Thêm sản phẩm mới
     * 
     * @param product Thông tin sản phẩm
     * @param images Danh sách đường dẫn hình ảnh
     * @param specifications Danh sách thông số kỹ thuật
     * @return ID sản phẩm nếu thành công, -1 nếu thất bại
     */
    public int addProduct(Product product, List<String> images, List<ProductSpecification> specifications) {
        return productDAO.addProduct(product, images, specifications);
    }
    
    /**
     * Cập nhật thông tin sản phẩm
     * 
     * @param product Thông tin sản phẩm cần cập nhật
     * @param images Danh sách đường dẫn hình ảnh mới (nếu có)
     * @param specifications Danh sách thông số kỹ thuật mới (nếu có)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateProduct(Product product, List<String> images, List<ProductSpecification> specifications) {
        return productDAO.updateProduct(product, images, specifications);
    }
    
    /**
     * Xóa sản phẩm
     * 
     * @param productId ID sản phẩm cần xóa
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean deleteProduct(int productId) {
        return productDAO.deleteProduct(productId);
    }
    
    /**
     * Kiểm tra sản phẩm có tồn tại không
     * 
     * @param productId ID sản phẩm
     * @return true nếu tồn tại, false nếu không
     */
    public boolean productExists(int productId) {
        return productDAO.productExists(productId);
    }
    
    /**
     * Kiểm tra người dùng có quyền chỉnh sửa sản phẩm không
     * 
     * @param productId ID sản phẩm
     * @param userId ID người dùng
     * @return true nếu có quyền, false nếu không
     */
    public boolean canUserEditProduct(int productId, int userId) {
        return productDAO.canUserEditProduct(productId, userId);
    }

    /**
     * Get bookmarked products for a user with pagination and sorting
     */
    public List<Map<String, Object>> getBookmarkedProducts(Integer userId, int page, int limit, String sort) {
        return productDAO.getBookmarkedProducts(userId, page, limit, sort);
    }

    /**
     * Get total count of bookmarked products for a user
     */
    public int getBookmarkedProductCount(Integer userId) {
        return productDAO.getBookmarkedProductCount(userId);
    }
}
