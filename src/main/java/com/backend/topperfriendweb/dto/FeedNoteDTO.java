package com.backend.topperfriendweb.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedNoteDTO {
    private String _id;
    private Long postgresUserId;
    private String title;
    private String pdfLink;
    private List<String> tags;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private Boolean userLiked;
    private Boolean userSaved;
}