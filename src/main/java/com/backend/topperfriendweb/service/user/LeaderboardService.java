package com.backend.topperfriendweb.service.user;

import com.backend.topperfriendweb.dto.userprofile.LeaderboardUserDTO;
import com.backend.topperfriendweb.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardUserDTO> getLeaderboard() {
        return getLeaderboard(100);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardUserDTO> getLeaderboard(int limit) {
        log.info("Fetching leaderboard with optimized query, limit={}", limit);

        // Single query to get users with their stats
        List<Object[]> userStats = leaderboardRepository.getUsersWithStats(limit);

        // Extract user IDs for top notes query
        List<Long> userIds = userStats.stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toList());

        // Single query to get top 3 notes for all users
        List<Object[]> topNotesData = leaderboardRepository.getTopNotesForUsers(userIds);

        // Group top notes by user ID
        Map<Long, List<LeaderboardUserDTO.TopNoteDTO>> topNotesMap = topNotesData.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.mapping(
                                row -> new LeaderboardUserDTO.TopNoteDTO(
                                        ((Number) row[1]).longValue(),
                                        (String) row[2],
                                        ((Number) row[3]).intValue()
                                ),
                                Collectors.toList()
                        )
                ));

        // Build the final result
        return userStats.stream()
                .map(row -> {
                    Long userId = ((Number) row[0]).longValue();
                    return new LeaderboardUserDTO(
                            userId,
                            (String) row[1], // username
                            (String) row[2], // name
                            (String) row[3], // image
                            (String) row[4], // collegeName
                            ((Number) row[5]).intValue(), // totalNotes
                            ((Number) row[6]).intValue(), // totalLikes
                            topNotesMap.getOrDefault(userId, new ArrayList<>())
                    );
                })
                .collect(Collectors.toList());
    }
}