package com.example.api.model;

import java.util.Date;

/**
 * Model u0111u1ea1i diu1ec7n cho u0111u00e1nh du1ea5u u0111u00e1nh giu00e1 lu00e0 hu1eefu u00edch
 */
public class ReviewHelpful {
    private int id;
    private int reviewId;
    private int userId;
    private boolean isHelpful;
    private Date createdAt;
    private Date updatedAt;
    
    public ReviewHelpful() {
    }
    
    public ReviewHelpful(int id, int reviewId, int userId, boolean isHelpful, Date createdAt, Date updatedAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.isHelpful = isHelpful;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isHelpful() {
        return isHelpful;
    }

    public void setHelpful(boolean isHelpful) {
        this.isHelpful = isHelpful;
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
}
