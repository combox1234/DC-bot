package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.LcSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

@Repository
public interface LcSubmissionRepository extends JpaRepository<LcSubmission, Long> {
    List<LcSubmission> findByDiscordId(String discordId);
    List<LcSubmission> findByDiscordIdOrderBySubmittedAtDesc(String discordId);
    long countByDiscordId(String discordId);

    @Query("SELECT COUNT(l) FROM LcSubmission l WHERE l.discordId = ?1 AND l.problemSlug = ?2 AND l.submittedAt >= ?3 AND l.submittedAt < ?4")
    long countTodaySolves(String discordId, String problemSlug, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT l.discordId, u.username, COUNT(l) as solveCount FROM LcSubmission l JOIN User u ON l.discordId = u.discordId WHERE l.submittedAt > ?1 GROUP BY l.discordId, u.username ORDER BY solveCount DESC")
    List<Object[]> findWeeklyLeaderboard(LocalDateTime since, Pageable pageable);
}
