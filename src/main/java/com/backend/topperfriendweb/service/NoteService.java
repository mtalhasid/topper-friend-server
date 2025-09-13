package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.CreateNoteRequest;
import com.backend.topperfriendweb.dto.NoteDTO;
import com.backend.topperfriendweb.dto.PaginationResponse;
import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        if ("upload".equals(pdfOption)) {
            if (!pdfLink.contains("cloudinary.com")) {
                throw new RuntimeException("Invalid Cloudinary URL");
            }
            return pdfLink;
        } else if ("link".equals(pdfOption)) {
            return processGoogleDriveLink(pdfLink);
        }
        throw new RuntimeException("Invalid pdfOption");
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

    // CONVERT RAW SQL RESULT TO DTO WITH USER'S LIKE/SAVE STATUS
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

        // Set user's like/save status based on SQL results
        Boolean userLiked = raw.length > 9 ? (Boolean) raw[9] : false;
        Boolean userSaved = raw.length > 10 ? (Boolean) raw[10] : false;

        // For frontend compatibility, populate arrays with just current user if they liked/saved
        if (currentUserId != null) {
            dto.setLikedByUsers(userLiked ? List.of(currentUserId) : List.of());
            dto.setSavedByUsers(userSaved ? List.of(currentUserId) : List.of());
        } else {
            dto.setLikedByUsers(List.of());
            dto.setSavedByUsers(List.of());
        }

        return dto;
    }

    public List<NoteDTO> getUserNotes(Long userId) {
        List<Object[]> rawResults = noteRepository.findUserNotesRaw(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    public void toggleLike(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

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
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (note.getSavedByUsers().contains(userId)) {
            note.getSavedByUsers().remove(userId);
        } else {
            note.getSavedByUsers().add(userId);
        }

        noteRepository.save(note);
    }

    public NoteDTO getNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return convertToDTO(note);
    }

    public void deleteNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this note");
        }

        noteRepository.delete(note);
    }

    public List<NoteDTO> getLikedNotes(Long userId) {
        List<Object[]> rawResults = noteRepository.findLikedNotesRaw(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    public List<NoteDTO> getSavedNotes(Long userId) {
        List<Object[]> rawResults = noteRepository.findSavedNotesRaw(userId);
        return rawResults.stream()
                .map(raw -> convertRawToDTO(raw, userId))
                .collect(Collectors.toList());
    }

    public List<Note> getUserNotesByUserId(Long userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // FULL DTO CONVERSION (ONLY FOR SINGLE NOTE DETAILS)
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