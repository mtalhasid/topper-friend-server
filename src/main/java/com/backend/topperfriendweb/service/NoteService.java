package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.note.CreateNoteRequest;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.note.PaginationResponse;
import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    public NoteDTO createNote(CreateNoteRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Note note = new Note();
        note.setUserId(request.getUserId());
        note.setTitle(request.getTitle());
        note.setPdfLink(processPdfLink(request.getPdfLink(), request.getPdfOption()));
        note.setTags(request.getTags());
        note.setUsername(user.getUsername());

        Note savedNote = noteRepository.save(note);
        return convertToDTO(savedNote);
    }

    private String processPdfLink(String pdfLink, String pdfOption) {
        if (pdfLink == null || pdfLink.trim().isEmpty()) {
            throw new IllegalArgumentException("PDF link is required");
        }

        if (pdfOption == null || pdfOption.trim().isEmpty()) {
            throw new IllegalArgumentException("PDF option is required");
        }

        if ("upload".equals(pdfOption)) {
            if (!pdfLink.contains("cloudinary.com")) {
                throw new IllegalArgumentException("Invalid Cloudinary URL");
            }
            return pdfLink;
        } else if ("link".equals(pdfOption)) {
            return processGoogleDriveLink(pdfLink);
        }
        throw new IllegalArgumentException("Invalid pdfOption. Must be 'upload' or 'link'");
    }

    private String processGoogleDriveLink(String url) {
        if (url.contains("drive.google.com")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\w-]{25,}");
            java.util.regex.Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return "https://drive.google.com/uc?id=" + matcher.group();
            }
        }
        return url;
    }

    // FAST VERSION - USES RAW SQL WITH CURRENT USER'S LIKE/SAVE STATUS
    public PaginationResponse<NoteDTO> browseNotes(String query, String tag, Integer page, Integer limit, Long currentUserId) {
        // Validate pagination parameters
        if (page == null || page < 1) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (limit == null || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }

        int offset = (page - 1) * limit;

        // SINGLE RAW SQL QUERY WITH USER'S LIKE/SAVE STATUS
        List<Object[]> rawResults = noteRepository.findNotesWithUserStatus(query, tag, currentUserId, limit, offset);

        List<NoteDTO> notes = rawResults.stream()
                .map(raw -> convertRawToDTO(raw, currentUserId))
                .collect(Collectors.toList());

        // FAST COUNT
        Long total = noteRepository.countNotesRaw(query, tag);

        // FAST TAGS
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

    private NoteDTO convertRawToDTO(Object[] raw, Long currentUserId) {
        NoteDTO dto = new NoteDTO();
        dto.set_id(raw[0].toString());
        dto.setPostgresUserId(((Number) raw[1]).longValue());
        dto.setTitle((String) raw[2]);
        dto.setPdfLink((String) raw[3]);
        dto.setLikes(((Number) raw[4]).intValue());
        dto.setCreatedAt(((java.sql.Timestamp) raw[5]).toLocalDateTime());
        dto.setUpdatedAt(((java.sql.Timestamp) raw[6]).toLocalDateTime());
        dto.setUsername((String) raw[7]);

        // Parse tags from comma-separated string
        String tagsStr = (String) raw[8];
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            dto.setTags(List.of());
        } else {
            dto.setTags(Arrays.asList(tagsStr.split(",")));
        }

        // Parse liked users from comma-separated string
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

        // Parse saved users from comma-separated string
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

    public List<NoteDTO> getUserNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        List<Object[]> rawResults = noteRepository.findUserNotesWithArrays(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    public void toggleLike(Long noteId, Long userId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (note.getLikedByUsers().contains(userId)) {
            note.getLikedByUsers().remove(userId);
            note.setLikes(note.getLikes() - 1);
        } else {
            note.getLikedByUsers().add(userId);
            note.setLikes(note.getLikes() + 1);
        }

        noteRepository.save(note);
    }

    public void toggleSave(Long noteId, Long userId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (note.getSavedByUsers().contains(userId)) {
            note.getSavedByUsers().remove(userId);
        } else {
            note.getSavedByUsers().add(userId);
        }

        noteRepository.save(note);
    }

    public NoteDTO getNoteById(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID is required");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        return convertToDTO(note);
    }

    public void deleteNote(Long noteId, Long userId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (!note.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to delete this note");
        }

        noteRepository.delete(note);
    }

    public List<NoteDTO> getLikedNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        List<Object[]> rawResults = noteRepository.findLikedNotesWithArrays(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    public List<NoteDTO> getSavedNotes(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        List<Object[]> rawResults = noteRepository.findSavedNotesWithArrays(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    private NoteDTO convertToDTO(Note note) {
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
}