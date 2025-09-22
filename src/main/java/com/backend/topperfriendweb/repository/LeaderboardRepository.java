package com.backend.topperfriendweb.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Repository
public class LeaderboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get all users with their note counts and total likes in a single query
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getUsersWithStats(int limit) {
        String sql = """
            SELECT 
                u.id,
                u.username,
                u.name,
                u.image,
                u.college_name,
                COUNT(n.id) as total_notes,
                COALESCE(SUM(n.likes), 0) as total_likes
            FROM users u
            LEFT JOIN notes n ON u.id = n.user_id
            WHERE u.onboarding_completed = true
            GROUP BY u.id, u.username, u.name, u.image, u.college_name
            HAVING COUNT(n.id) > 0
            ORDER BY total_likes DESC
        """;

        return entityManager.createNativeQuery(sql)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Get top 3 notes for each user in the provided list
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopNotesForUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        String sql = """
            SELECT user_id, id, title, likes FROM (
                SELECT 
                    n.user_id,
                    n.id,
                    n.title,
                    n.likes,
                    ROW_NUMBER() OVER (PARTITION BY n.user_id ORDER BY n.likes DESC, n.id ASC) AS rn
                FROM notes n
                WHERE n.user_id IN :userIds
            ) t
            WHERE t.rn <= 3
            ORDER BY user_id, likes DESC
        """;

        return entityManager.createNativeQuery(sql)
                .setParameter("userIds", userIds)
                .getResultList();
    }
}