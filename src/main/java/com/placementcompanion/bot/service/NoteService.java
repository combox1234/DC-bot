package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.UserNote;
import com.placementcompanion.bot.repository.UserNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final UserNoteRepository userNoteRepository;
    private final UserService userService;

    public NoteService(UserNoteRepository userNoteRepository, UserService userService) {
        this.userNoteRepository = userNoteRepository;
        this.userService = userService;
    }

    @Transactional
    public UserNote addNote(String discordId, String username, String title, String content) {
        // Ensure user exists
        userService.ensureUser(discordId, username);

        // Check if note with same title exists
        if (userNoteRepository.existsByDiscordIdAndTitleIgnoreCase(discordId, title)) {
            throw new IllegalArgumentException("A note with the title '" + title + "' already exists.");
        }

        UserNote note = new UserNote(discordId, title, content);
        return userNoteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<UserNote> getNotesForUser(String discordId) {
        return userNoteRepository.findByDiscordId(discordId);
    }

    @Transactional(readOnly = true)
    public Optional<UserNote> getNoteByTitle(String discordId, String title) {
        return userNoteRepository.findByDiscordIdAndTitleIgnoreCase(discordId, title);
    }

    @Transactional
    public boolean deleteNoteByTitle(String discordId, String title) {
        Optional<UserNote> noteOpt = userNoteRepository.findByDiscordIdAndTitleIgnoreCase(discordId, title);
        if (noteOpt.isPresent()) {
            userNoteRepository.delete(noteOpt.get());
            return true;
        }
        return false;
    }
}
