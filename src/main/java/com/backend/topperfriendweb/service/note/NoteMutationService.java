package com.backend.topperfriendweb.service.note;

import com.backend.topperfriendweb.dto.note.CreateNoteRequest;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.mapper.NoteMapper;
import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class NoteMutationService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;

    public NoteMutationService(NoteRepository noteRepository,
                               UserRepository userRepository,
                               NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.noteMapper = noteMapper;
    }

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
        return noteMapper.toDTO(savedNote);
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
        return noteMapper.toDTO(note);
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
}
