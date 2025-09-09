// src/main/java/com/backend/topperfriendweb/dto/NoteDTO.java
package com.backend.topperfriendweb.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteDTO {
    private String _id;
    private Long postgresUserId;
    private String title;
    private String pdfLink;
    private List<String> tags;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private List<Long> likedByUsers;
    private List<Long> savedByUsers;

    // Getters and Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Long getPostgresUserId() {
        return postgresUserId;
    }

    public void setPostgresUserId(Long postgresUserId) {
        this.postgresUserId = postgresUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<Long> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public List<Long> getSavedByUsers() {
        return savedByUsers;
    }

    public void setSavedByUsers(List<Long> savedByUsers) {
        this.savedByUsers = savedByUsers;
    }
}