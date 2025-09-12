package com.backend.topperfriendweb.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "collection_items")
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_id")
    @JsonBackReference // Back reference to prevent circular JSON
    private Collection collection;

    @NotBlank
    @Column(nullable = false)
    private String itemType; // "NOTE" or "STUDY_PLAN"

    @NotNull
    @Column(nullable = false)
    private Long itemId;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}