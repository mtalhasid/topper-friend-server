package com.backend.topperfriendweb.service.note;

import com.backend.topperfriendweb.dto.note.CreateNoteRequest;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.note.PaginationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private final NoteQueryService noteQueryService;
    private final NoteMutationService noteMutationService;

    public NoteService(NoteQueryService noteQueryService, NoteMutationService noteMutationService) {
        this.noteQueryService = noteQueryService;
        this.noteMutationService = noteMutationService;
    }

    public NoteDTO createNote(CreateNoteRequest request) {
        return noteMutationService.createNote(request);
    }

    // USES RAW SQL WITH CURRENT USER'S LIKE/SAVE STATUS
    public PaginationResponse<NoteDTO> browseNotes(String query, String tag, Integer page, Integer limit, Long currentUserId) {
        return noteQueryService.browseNotes(query, tag, page, limit, currentUserId);
    }

    public List<NoteDTO> getUserNotes(Long userId) {
        return noteQueryService.getUserNotes(userId);
    }

    public void toggleLike(Long noteId, Long userId) {
        noteMutationService.toggleLike(noteId, userId);
    }

    public void toggleSave(Long noteId, Long userId) {
        noteMutationService.toggleSave(noteId, userId);
    }

    public NoteDTO getNoteById(Long noteId) {
        return noteMutationService.getNoteById(noteId);
    }

    public void deleteNote(Long noteId, Long userId) {
        noteMutationService.deleteNote(noteId, userId);
    }

    public List<NoteDTO> getLikedNotes(Long userId) {
        return noteQueryService.getLikedNotes(userId);
    }

    public List<NoteDTO> getSavedNotes(Long userId) {
        return noteQueryService.getSavedNotes(userId);
    }
}