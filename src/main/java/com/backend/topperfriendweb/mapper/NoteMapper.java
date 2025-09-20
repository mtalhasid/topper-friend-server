package com.backend.topperfriendweb.mapper;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.model.Note;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoteMapper {

    public NoteDTO toDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.set_id(note.getId().toString());
        dto.setPostgresUserId(note.getUserId());
        dto.setTitle(note.getTitle());
        dto.setPdfLink(note.getPdfLink());
        dto.setTags(note.getTags());
        dto.setLikes(note.getLikes());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setUsername(note.getUsername());
        dto.setLikedByUsers(note.getLikedByUsers());
        dto.setSavedByUsers(note.getSavedByUsers());
        return dto;
    }

    // Maps raw SQL result used by NoteRepository.find* methods to NoteDTO
    public NoteDTO rawToDTO(Object[] raw, Long currentUserId) {
        NoteDTO dto = new NoteDTO();
        dto.set_id(raw[0].toString());
        dto.setPostgresUserId(((Number) raw[1]).longValue());
        dto.setTitle((String) raw[2]);
        dto.setPdfLink((String) raw[3]);
        dto.setLikes(((Number) raw[4]).intValue());
        dto.setCreatedAt(((Timestamp) raw[5]).toLocalDateTime());
        dto.setUpdatedAt(((Timestamp) raw[6]).toLocalDateTime());
        dto.setUsername((String) raw[7]);

        String tagsStr = (String) raw[8];
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            dto.setTags(List.of());
        } else {
            dto.setTags(Arrays.asList(tagsStr.split(",")));
        }

        String likedUsersStr = (String) raw[9];
        if (likedUsersStr == null || likedUsersStr.trim().isEmpty()) {
            dto.setLikedByUsers(List.of());
        } else {
            dto.setLikedByUsers(
                    Arrays.stream(likedUsersStr.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList())
            );
        }

        String savedUsersStr = (String) raw[10];
        if (savedUsersStr == null || savedUsersStr.trim().isEmpty()) {
            dto.setSavedByUsers(List.of());
        } else {
            dto.setSavedByUsers(
                    Arrays.stream(savedUsersStr.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
