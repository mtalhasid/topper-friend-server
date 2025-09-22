package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.userprofile.LeaderboardUserDTO;
import com.backend.topperfriendweb.service.user.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/leaderboard")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<LeaderboardUserDTO>>> getLeaderboard(
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) Integer limit
    ) {
        log.info("Fetching leaderboard data, limit={}", limit);
        List<LeaderboardUserDTO> leaderboard = leaderboardService.getLeaderboard(limit);
        return ResponseEntity.ok(
                CommonResponse.success("Leaderboard retrieved successfully", leaderboard)
        );
    }
}