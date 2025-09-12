package com.backend.topperfriendweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;  // used by service layer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;    // JPA relationship

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 2048)
    private String pdfLink;

    @ElementCollection
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @NotBlank
    @Column(nullable = false)
    private String username;

    @ElementCollection
    @CollectionTable(name = "note_liked_users", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "user_id")
    @Builder.Default
    private List<Long> likedByUsers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "note_saved_users", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "user_id")
    @Builder.Default
    private List<Long> savedByUsers = new ArrayList<>();
}
