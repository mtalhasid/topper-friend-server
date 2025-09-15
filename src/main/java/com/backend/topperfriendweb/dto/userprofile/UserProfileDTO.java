package com.backend.topperfriendweb.dto.userprofile;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String collegeName;
    private String rollNumber;
    private String image;
    private Boolean onboardingCompleted;
    private LocalDateTime emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NoteDTO> notes;
    private Integer totalNotes;
    private Integer totalLikes;
}
