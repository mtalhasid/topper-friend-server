package com.backend.topperfriendweb.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "collection_items")
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    @JsonBackReference // Back reference to prevent circular JSON
    private Collection collection;

    private String itemType; // "NOTE" or "STUDY_PLAN"
    private Long itemId;

    private LocalDateTime addedAt = LocalDateTime.now();
}