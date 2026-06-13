package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.ChallengeCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeCompletionRepository extends JpaRepository<ChallengeCompletion, Long> {
    Optional<ChallengeCompletion> findByDiscordIdAndCompletedDate(String discordId, LocalDate completedDate);
    List<ChallengeCompletion> findByDiscordId(String discordId);
    boolean existsByDiscordIdAndChallengeId(String discordId, String challengeId);
}
