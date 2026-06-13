package com.placementcompanion.bot;

import com.placementcompanion.bot.entity.UserNote;
import com.placementcompanion.bot.repository.UserNoteRepository;
import com.placementcompanion.bot.service.NoteService;
import com.placementcompanion.bot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    @Mock
    private UserNoteRepository userNoteRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddNote_Success() {
        when(userNoteRepository.existsByDiscordIdAndTitleIgnoreCase("123", "Java Tricks")).thenReturn(false);
        when(userNoteRepository.save(any(UserNote.class))).thenAnswer(i -> i.getArguments()[0]);

        UserNote saved = noteService.addNote("123", "user1", "Java Tricks", "Use Stream API");

        assertNotNull(saved);
        assertEquals("123", saved.getDiscordId());
        assertEquals("Java Tricks", saved.getTitle());
        assertEquals("Use Stream API", saved.getContent());
        verify(userService, times(1)).ensureUser("123", "user1");
        verify(userNoteRepository, times(1)).save(any(UserNote.class));
    }

    @Test
    void testAddNote_DuplicateTitle() {
        when(userNoteRepository.existsByDiscordIdAndTitleIgnoreCase("123", "Java Tricks")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            noteService.addNote("123", "user1", "Java Tricks", "Use Stream API");
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userNoteRepository, never()).save(any(UserNote.class));
    }

    @Test
    void testGetNotesForUser() {
        List<UserNote> notes = Arrays.asList(
            new UserNote("123", "Title1", "Content1"),
            new UserNote("123", "Title2", "Content2")
        );
        when(userNoteRepository.findByDiscordId("123")).thenReturn(notes);

        List<UserNote> result = noteService.getNotesForUser("123");
        assertEquals(2, result.size());
    }

    @Test
    void testGetNoteByTitle() {
        UserNote note = new UserNote("123", "Title1", "Content1");
        when(userNoteRepository.findByDiscordIdAndTitleIgnoreCase("123", "Title1")).thenReturn(Optional.of(note));

        Optional<UserNote> result = noteService.getNoteByTitle("123", "Title1");
        assertTrue(result.isPresent());
        assertEquals("Title1", result.get().getTitle());
    }

    @Test
    void testDeleteNoteByTitle_Success() {
        UserNote note = new UserNote("123", "Title1", "Content1");
        when(userNoteRepository.findByDiscordIdAndTitleIgnoreCase("123", "Title1")).thenReturn(Optional.of(note));

        boolean deleted = noteService.deleteNoteByTitle("123", "Title1");
        assertTrue(deleted);
        verify(userNoteRepository, times(1)).delete(note);
    }

    @Test
    void testDeleteNoteByTitle_NotFound() {
        when(userNoteRepository.findByDiscordIdAndTitleIgnoreCase("123", "Title1")).thenReturn(Optional.empty());

        boolean deleted = noteService.deleteNoteByTitle("123", "Title1");
        assertFalse(deleted);
        verify(userNoteRepository, never()).delete(any(UserNote.class));
    }
}
