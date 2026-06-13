package com.placementcompanion.bot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "challenge_completion")
public class ChallengeCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discordId;
    private String challengeId;
    private LocalDate completedDate;

    public ChallengeCompletion() {}

    public ChallengeCompletion(String discordId, String challengeId, LocalDate completedDate) {
        this.discordId = discordId;
        this.challengeId = challengeId;
        this.completedDate = completedDate;
    }

    public Long getId() { return id; }
    public String getDiscordId() { return discordId; }
    public String getChallengeId() { return challengeId; }
    public LocalDate getCompletedDate() { return completedDate; }
}
