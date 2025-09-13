package com.backend.topperfriendweb.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collection_items")
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonBackReference // Back reference to prevent circular JSON
    private Collection collection;

    @NotBlank
    @Column(nullable = false)
    private String itemType; // "NOTE" or "STUDY_PLAN"

    @NotNull
    @Column(nullable = false)
    private Long itemId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    // Constructors
    public CollectionItem() {}

    // Pre-persist method
    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}