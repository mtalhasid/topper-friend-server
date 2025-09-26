package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.note.CreateNoteRequest;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.note.PaginationResponse;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.note.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Long getCurrentUserId() {
        try {
            return getLoggedInUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping
    public ResponseEntity<CommonResponse<NoteDTO>> createNote(@Valid @RequestBody CreateNoteRequest request) {
        User user = getLoggedInUser();
        request.setUserId(user.getId());
        NoteDTO note = noteService.createNote(request);
        return ResponseEntity.status(201)
                .body(CommonResponse.success("Note created successfully", note));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<NoteDTO>>> getNotes() {
        User user = getLoggedInUser();
        List<NoteDTO> notes = noteService.getUserNotes(user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("User notes retrieved successfully", notes)
        );
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<CommonResponse<NoteDTO>> getNoteById(@PathVariable Long noteId) {
        NoteDTO note = noteService.getNoteById(noteId);
        return ResponseEntity.ok(
                CommonResponse.success("Note retrieved successfully", note)
        );
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<CommonResponse<String>> deleteNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.deleteNote(noteId, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Note deleted successfully", null)
        );
    }

    @GetMapping("/browse")
    public ResponseEntity<CommonResponse<PaginationResponse<NoteDTO>>> browseNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        Long currentUserId = getCurrentUserId();
        PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
        return ResponseEntity.ok(
                CommonResponse.success("Notes browsed successfully", results)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PaginationResponse<NoteDTO>>> searchNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        Long currentUserId = getCurrentUserId();
        PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
        return ResponseEntity.ok(
                CommonResponse.success("Notes searched successfully", results)
        );
    }

    @GetMapping("/page")
    public ResponseEntity<CommonResponse<PaginationResponse<NoteDTO>>> getPaginatedNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        Long currentUserId = getCurrentUserId();
        PaginationResponse<NoteDTO> results = noteService.browseNotes(query, tag, page, limit, currentUserId);
        return ResponseEntity.ok(
                CommonResponse.success("Paginated notes retrieved successfully", results)
        );
    }

    @GetMapping("/saved")
    public ResponseEntity<CommonResponse<List<NoteDTO>>> getSavedNotes() {
        User user = getLoggedInUser();
        List<NoteDTO> notes = noteService.getSavedNotes(user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Saved notes retrieved successfully", notes)
        );
    }

    @GetMapping("/liked")
    public ResponseEntity<CommonResponse<List<NoteDTO>>> getLikedNotes() {
        User user = getLoggedInUser();
        List<NoteDTO> notes = noteService.getLikedNotes(user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Liked notes retrieved successfully", notes)
        );
    }

    @PostMapping("/{noteId}/like")
    public ResponseEntity<CommonResponse<String>> likeNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleLike(noteId, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Note liked successfully", null)
        );
    }

    @DeleteMapping("/{noteId}/like")
    public ResponseEntity<CommonResponse<String>> unlikeNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleLike(noteId, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Note unliked successfully", null)
        );
    }

    @PostMapping("/{noteId}/save")
    public ResponseEntity<CommonResponse<String>> saveNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleSave(noteId, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Note saved successfully", null)
        );
    }

    @DeleteMapping("/{noteId}/save")
    public ResponseEntity<CommonResponse<String>> unsaveNote(@PathVariable Long noteId) {
        User user = getLoggedInUser();
        noteService.toggleSave(noteId, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Note unsaved successfully", null)
        );
    }
}