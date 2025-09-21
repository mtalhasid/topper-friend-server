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

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) value).toLocalDateTime();
        if (value instanceof java.time.ZonedDateTime) return ((java.time.ZonedDateTime) value).toLocalDateTime();
        try { return java.time.LocalDateTime.parse(value.toString()); } catch (Exception ignored) { return null; }
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsersWithCompletedOnboarding() {
        int limit = 50;
        int offset = 0;
        List<Object[]> rows = userRepository.findAllBasic(limit, offset);
        return rows.stream().map(r -> {
            User u = new User();
            u.setId(((Number) r[0]).longValue());
            u.setUsername((String) r[1]);
            u.setName((String) r[2]);
            u.setImage((String) r[3]);
            u.setCollegeName((String) r[4]);
            u.setCreatedAt(toLocalDateTime(r[5]));
            // onboardingCompleted is true by query condition
            u.setOnboardingCompleted(true);
            return u;
        }).toList();
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
        int limit = 20;
        List<Object[]> rows = userRepository.searchBasic(query.trim(), limit);
        return rows.stream().map(r -> {
            User u = new User();
            u.setId(((Number) r[0]).longValue());
            u.setUsername((String) r[1]);
            u.setName((String) r[2]);
            u.setImage((String) r[3]);
            u.setCollegeName((String) r[4]);
            u.setCreatedAt(toLocalDateTime(r[5]));
            return u;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByCollege(String collegeName) {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            throw new IllegalArgumentException("College name cannot be empty");
        }
        return userRepository.findByCollegeNameContainingIgnoreCase(collegeName);
    }
}