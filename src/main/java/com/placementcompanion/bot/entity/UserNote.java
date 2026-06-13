package com.placementcompanion.bot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notes")
public class UserNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String discordId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 4096)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UserNote() {}

    public UserNote(String discordId, String title, String content) {
        this.discordId = discordId;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getDiscordId() { return discordId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}
