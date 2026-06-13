package com.placementcompanion.bot.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    private String discordId;

    private String reminderTime; // HH:mm format

    private boolean enabled;

    public Reminder() {}

    public Reminder(String discordId, String reminderTime, boolean enabled) {
        this.discordId = discordId;
        this.reminderTime = reminderTime;
        this.enabled = enabled;
    }

    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }
    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
