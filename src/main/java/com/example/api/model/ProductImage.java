package com.example.api.model;

import java.util.Date;

/**
 * Lớp đại diện cho hình ảnh sản phẩm
 */
public class ProductImage {
    private int id;
    private int productId;
    private String imagePath;
    private boolean isPrimary;
    private int sortOrder;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public ProductImage() {
    }
    
    public ProductImage(int productId, String imagePath, boolean isPrimary, int sortOrder) {
        this.productId = productId;
        this.imagePath = imagePath;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }
    
    // Getters và Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
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
    
    @Override
    public String toString() {
        return "ProductImage{" +
                "id=" + id +
                ", productId=" + productId +
                ", imagePath='" + imagePath + '\'' +
                ", isPrimary=" + isPrimary +
                ", sortOrder=" + sortOrder +
                "}";
    }
}
