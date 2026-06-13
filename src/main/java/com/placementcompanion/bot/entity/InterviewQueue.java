package com.placementcompanion.bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_queue")
public class InterviewQueue {

    @Id
    private String discordId;

    private LocalDateTime joinedAt;

    public InterviewQueue() {
    }

    public InterviewQueue(String discordId, LocalDateTime joinedAt) {
        this.discordId = discordId;
        this.joinedAt = joinedAt;
    }

    public String getDiscordId() {
        return discordId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
