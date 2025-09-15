package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.note.CreateNoteRequest;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.note.PaginationResponse;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Long getCurrentUserId() {
        try {
            return getLoggedInUser().getId();
        } catch (Exception e) {
            return null; // User not logged in
        }
    }

    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody CreateNoteRequest request) {
        try {
            User user = getLoggedInUser();
            request.setUserId(user.getId());
            NoteDTO note = noteService.createNote(request);
            return ResponseEntity.status(201).body(note);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getNotes() {
        try {
            User user = getLoggedInUser();
            List<NoteDTO> notes = noteService.getUserNotes(user.getId());
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId) {
        try {
            NoteDTO note = noteService.getNoteById(noteId);
            return ResponseEntity.ok(note);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId) {
        try {
            User user = getLoggedInUser();
            noteService.deleteNote(noteId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Note deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> browseNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Long currentUserId = getCurrentUserId(); // Get current user ID
            PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long currentUserId = getCurrentUserId();
            PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/page")
    public ResponseEntity<?> getPaginatedNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long currentUserId = getCurrentUserId();
            PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedNotes() {
        try {
            User user = getLoggedInUser();
            List<NoteDTO> notes = noteService.getSavedNotes(user.getId());
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/liked")
    public ResponseEntity<?> getLikedNotes() {
        try {
            User user = getLoggedInUser();
            List<NoteDTO> notes = noteService.getLikedNotes(user.getId());
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{noteId}/like")
    public ResponseEntity<?> likeNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleLike(noteId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Note liked successfully"));
    }

    @DeleteMapping("/{noteId}/like")
    public ResponseEntity<?> unlikeNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleLike(noteId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Note unliked successfully"));
    }

    @PostMapping("/{noteId}/save")
    public ResponseEntity<?> saveNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleSave(noteId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Note saved successfully"));
    }

    @DeleteMapping("/{noteId}/save")
    public ResponseEntity<?> unsaveNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleSave(noteId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Note unsaved successfully"));
    }
}