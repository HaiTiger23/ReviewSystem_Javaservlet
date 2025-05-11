package com.example.api.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Lớp đại diện cho sản phẩm trong hệ thống
 */
public class Product {
    private int id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private int categoryId;
    private int userId;
    private double rating;
    private int reviewCount;
    private Date createdAt;
    private Date updatedAt;
    
    // Các trường bổ sung không lưu trực tiếp trong bảng products
    private String categoryName;
    private List<String> images;
    private List<ProductSpecification> specifications;
    private boolean isBookmarked;
    private boolean isReviewed;
    
    // Constructors
    public Product() {
    }
    
    // Getters và Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public List<ProductSpecification> getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(List<ProductSpecification> specifications) {
        this.specifications = specifications;
    }
    
    public boolean isBookmarked() {
        return isBookmarked;
    }
    
    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
    
    public boolean isReviewed() {
        return isReviewed;
    }
    
    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                ", rating=" + rating +
                ", reviewCount=" + reviewCount +
                "}";
    }
}
