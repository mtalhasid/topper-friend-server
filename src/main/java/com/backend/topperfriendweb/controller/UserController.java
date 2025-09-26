package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsersWithCompletedOnboarding();
        return ResponseEntity.ok(CommonResponse.success("Users retrieved successfully", users));
    }

    // Get user profile by username WITH their notes
    @GetMapping("/{username}")
    public ResponseEntity<CommonResponse<UserProfileDTO>> getUserProfile(@PathVariable String username) {
        UserProfileDTO userProfile = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(CommonResponse.success("User profile retrieved successfully", userProfile));
    }

    // Search users by username or name
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<User>>> searchUsers(@RequestParam String q) {
        List<User> users = userService.searchUsers(q);
        return ResponseEntity.ok(CommonResponse.success("Search completed successfully", users));
    }

    // Get users by college
    @GetMapping("/college/{collegeName}")
    public ResponseEntity<CommonResponse<List<User>>> getUsersByCollege(@PathVariable String collegeName) {
        List<User> users = userService.getUsersByCollege(collegeName);
        return ResponseEntity.ok(CommonResponse.success("Users from college retrieved successfully", users));
    }
}