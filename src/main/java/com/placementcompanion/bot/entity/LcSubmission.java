package com.placementcompanion.bot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lc_submissions")
public class LcSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discordId;
    private String problemSlug;
    private String difficulty;
    private LocalDateTime submittedAt;

    public LcSubmission() {}

    public LcSubmission(String discordId, String problemSlug, String difficulty) {
        this.discordId = discordId;
        this.problemSlug = problemSlug;
        this.difficulty = difficulty;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getDiscordId() { return discordId; }
    public String getProblemSlug() { return problemSlug; }
    public String getDifficulty() { return difficulty; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
