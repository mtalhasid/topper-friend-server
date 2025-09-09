// src/main/java/com/backend/topperfriendweb/service/NoteService.java
package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.CreateNoteRequest;
import com.backend.topperfriendweb.dto.NoteDTO;
import com.backend.topperfriendweb.dto.PaginationResponse;
import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
            // Extract file ID logic
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\w-]{25,}");
            java.util.regex.Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return "https://drive.google.com/uc?id=" + matcher.group();
            }
        }
        return url;
    }

    public List<NoteDTO> getUserNotes(Long userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaginationResponse<NoteDTO> browseNotes(String query, String tag, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        List<Note> notes = noteRepository.searchNotes(query, tag);
        long total = notes.size();

        // Manual pagination since we're using custom query
        List<NoteDTO> paginatedNotes = notes.stream()
                .skip((page - 1) * (long) limit)
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        List<String> allTags = noteRepository.findAllDistinctTags();

        PaginationResponse<NoteDTO> response = new PaginationResponse<>();
        response.setNotes(paginatedNotes);
        response.setTags(allTags);

        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo();
        pagination.setTotal(total);
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotalPages((int) Math.ceil((double) total / limit));

        response.setPagination(pagination);

        return response;
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

    // Add this method to your NoteService class
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
        return noteRepository.findByLikedByUsersContainsOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NoteDTO> getSavedNotes(Long userId) {
        return noteRepository.findBySavedByUsersContainsOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
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