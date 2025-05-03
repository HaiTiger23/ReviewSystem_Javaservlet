package com.example.api.model;

import java.util.Date;

/**
 * Lớp đại diện cho thông số kỹ thuật của sản phẩm
 */
public class ProductSpecification {
    private int id;
    private int productId;
    private String name;
    private String value;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public ProductSpecification() {
    }
    
    public ProductSpecification(String name, String value) {
        this.name = name;
        this.value = value;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
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
        return name + ": " + value;
    }
}
