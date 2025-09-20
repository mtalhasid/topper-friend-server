package com.backend.topperfriendweb.mapper;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.note.NoteService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private final NoteService noteService;

    public UserMapper(NoteService noteService) {
        this.noteService = noteService;
    }

    public UserProfileDTO toUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCollegeName(user.getCollegeName());
        dto.setRollNumber(user.getRollNumber());
        dto.setImage(user.getImage());
        dto.setOnboardingCompleted(user.getOnboardingCompleted());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        List<NoteDTO> userNotes = noteService.getUserNotes(user.getId());
        dto.setNotes(userNotes);
        dto.setTotalNotes(userNotes.size());
        dto.setTotalLikes(userNotes.stream().mapToInt(NoteDTO::getLikes).sum());

        return dto;
    }
}
