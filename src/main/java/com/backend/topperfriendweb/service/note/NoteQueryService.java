package com.backend.topperfriendweb.service.note;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.note.PaginationResponse;
import com.backend.topperfriendweb.mapper.NoteMapper;
import com.backend.topperfriendweb.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteQueryService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public NoteQueryService(NoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }
    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) value).toLocalDateTime();
        if (value instanceof java.time.ZonedDateTime) return ((java.time.ZonedDateTime) value).toLocalDateTime();
        try { return java.time.LocalDateTime.parse(value.toString()); } catch (Exception ignored) { return null; }
    }

    // USES RAW SQL WITH CURRENT USER'S LIKE/SAVE STATUS
    public PaginationResponse<NoteDTO> browseNotes(String query, String tag, Integer page, Integer limit, Long currentUserId) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (limit == null || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        int offset = (page - 1) * limit;

        List<Object[]> rawResults = noteRepository.findNotesWithUserStatus(query, tag, currentUserId, limit, offset);
        List<NoteDTO> notes = rawResults.stream()
                .map(raw -> noteMapper.rawToDTO(raw, currentUserId))
                .collect(Collectors.toList());

        Long total = noteRepository.countNotesRaw(query, tag);
        List<String> allTags = noteRepository.findAllTagsFast();

        PaginationResponse<NoteDTO> response = new PaginationResponse<>();
        response.setNotes(notes);
        response.setTags(allTags);

        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo();
        pagination.setTotal(total);
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotalPages((int) Math.ceil((double) total / limit));
        response.setPagination(pagination);
        return response;
    }

    public List<NoteDTO> getUserNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        // Lightweight path for profile: no joins/aggregations
        List<Object[]> rows = noteRepository.findUserNotesBasic(userId);
        return rows.stream().map(r -> {
            try {
                NoteDTO dto = new NoteDTO();
                Long id = ((Number) r[0]).longValue();
                dto.set_id(id.toString());
                dto.setPostgresUserId(((Number) r[1]).longValue());
                dto.setTitle((String) r[2]);
                dto.setPdfLink((String) r[3]);
                dto.setLikes(r[4] == null ? 0 : ((Number) r[4]).intValue());
                dto.setCreatedAt(toLocalDateTime(r[5]));
                dto.setUpdatedAt(toLocalDateTime(r[6]));
                dto.setUsername((String) r[7]);
                dto.setTags(java.util.Collections.emptyList());
                dto.setLikedByUsers(java.util.Collections.emptyList());
                dto.setSavedByUsers(java.util.Collections.emptyList());
                return dto;
            } catch (Exception ex) {
                // If a single row fails to map, skip it rather than failing the page
                return null;
            }
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    public List<NoteDTO> getLikedNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        List<Object[]> rawResults = noteRepository.findLikedNotesWithArrays(userId);
        return rawResults.stream().map(raw -> noteMapper.rawToDTO(raw, userId)).collect(Collectors.toList());
    }

    public List<NoteDTO> getSavedNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        List<Object[]> rawResults = noteRepository.findSavedNotesWithArrays(userId);
        return rawResults.stream().map(raw -> noteMapper.rawToDTO(raw, userId)).collect(Collectors.toList());
    }
}
