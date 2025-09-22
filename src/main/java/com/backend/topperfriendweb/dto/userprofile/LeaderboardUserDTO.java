package com.backend.topperfriendweb.dto.userprofile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardUserDTO {
    private Long id;
    private String username;
    private String name;
    private String image;
    private String collegeName;
    private Integer totalNotes;
    private Integer totalLikes;
    private List<TopNoteDTO> topNotes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopNoteDTO {
        private Long id;
        private String title;
        private Integer likes;
    }
}