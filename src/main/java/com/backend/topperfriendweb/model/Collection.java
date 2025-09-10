package com.backend.topperfriendweb.model;
// Add these annotations to prevent circular references
import com.backend.topperfriendweb.model.CollectionItem;
import com.backend.topperfriendweb.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // Prevent circular reference
    private User user;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Manage the reference
    private List<CollectionItem> items = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Add this getter to expose user ID without the whole user object
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}