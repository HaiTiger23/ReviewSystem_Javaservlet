package com.example.api.model;

import java.util.Date;

/**
 * Lớp đại diện cho danh mục sản phẩm
 */
public class Category {
    private int id;
    private String name;
    private String slug;
    private Integer parentId;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public Category() {
    }
    
    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
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
    
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", parentId=" + parentId +
                "}";
    }
}
