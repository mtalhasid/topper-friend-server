package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final NoteService noteService;

    @Transactional(readOnly = true)
    public List<User> getAllUsersWithCompletedOnboarding() {
        return userRepository.findByOnboardingCompletedTrue();
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getOnboardingCompleted()) {
            throw new RuntimeException("User profile not completed");
        }

        // Get user's notes
        List<NoteDTO> userNotes = noteService.getUserNotes(user.getId());
        int totalLikes = userNotes.stream().mapToInt(NoteDTO::getLikes).sum();

        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getCollegeName(),
                user.getRollNumber(),
                user.getImage(),
                user.getOnboardingCompleted(),
                user.getEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                userNotes,
                userNotes.size(),
                totalLikes
        );
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByCollege(String collegeName) {
        return userRepository.findByCollegeNameContainingIgnoreCase(collegeName);
    }
}
