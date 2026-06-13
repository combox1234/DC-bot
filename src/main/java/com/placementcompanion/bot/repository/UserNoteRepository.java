package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByDiscordId(String discordId);
    Optional<UserNote> findByDiscordIdAndTitleIgnoreCase(String discordId, String title);
    boolean existsByDiscordIdAndTitleIgnoreCase(String discordId, String title);
}
