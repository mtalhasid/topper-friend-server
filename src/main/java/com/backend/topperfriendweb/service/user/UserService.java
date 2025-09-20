package com.backend.topperfriendweb.service.user;

import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<User> getAllUsersWithCompletedOnboarding() {
        return userRepository.findByOnboardingCompletedTrue();
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getOnboardingCompleted()) {
            throw new IllegalArgumentException("User profile not completed");
        }

        // Delegate to mapper to build full profile with notes and aggregates
        return userMapper.toUserProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByCollege(String collegeName) {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            throw new IllegalArgumentException("College name cannot be empty");
        }
        return userRepository.findByCollegeNameContainingIgnoreCase(collegeName);
    }
}