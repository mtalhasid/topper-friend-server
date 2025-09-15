// src/main/java/com/backend/topperfriendweb/dto/NoteDTO.java
package com.backend.topperfriendweb.dto.note;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}