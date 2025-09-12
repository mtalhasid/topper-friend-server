package com.backend.topperfriendweb.dto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// CollectionDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDTO {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CollectionItemDTO> items;
}