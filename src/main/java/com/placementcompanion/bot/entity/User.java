package com.placementcompanion.bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String discordId;
    private String username;
    private int xp = 0;
    private int level = 1;
    private int currentStreak = 0;
    private int bestStreak = 0;
    private LocalDateTime lastActivity;
    private LocalDateTime joinedAt;
    private String leetcodeHandle;
    private String githubHandle;

    public User() {}

    public User(String discordId, String username) {
        this.discordId = discordId;
        this.username = username;
        this.joinedAt = LocalDateTime.now();
    }

    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public String getLeetcodeHandle() { return leetcodeHandle; }
    public void setLeetcodeHandle(String leetcodeHandle) { this.leetcodeHandle = leetcodeHandle; }

    public String getGithubHandle() { return githubHandle; }
    public void setGithubHandle(String githubHandle) { this.githubHandle = githubHandle; }
}
