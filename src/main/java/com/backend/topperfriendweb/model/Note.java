package com.backend.topperfriendweb.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // KEEP this for your service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;    // KEEP this for the JPA relationship

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2048)
    private String pdfLink;

    @ElementCollection
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private Integer likes = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String username;

    @ElementCollection
    @CollectionTable(name = "note_liked_users", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "user_id")
    private List<Long> likedByUsers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "note_saved_users", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "user_id")
    private List<Long> savedByUsers = new ArrayList<>();

    // Constructors
    public Note() {}

    public Note(Long userId, String title, String pdfLink, List<String> tags, String username) {
        this.userId = userId;
        this.title = title;
        this.pdfLink = pdfLink;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.username = username;
        this.likes = 0;
        this.likedByUsers = new ArrayList<>();
        this.savedByUsers = new ArrayList<>();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPdfLink() { return pdfLink; }
    public void setPdfLink(String pdfLink) { this.pdfLink = pdfLink; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<Long> getLikedByUsers() { return likedByUsers; }
    public void setLikedByUsers(List<Long> likedByUsers) { this.likedByUsers = likedByUsers; }

    public List<Long> getSavedByUsers() { return savedByUsers; }
    public void setSavedByUsers(List<Long> savedByUsers) { this.savedByUsers = savedByUsers; }
}
