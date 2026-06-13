package com.placementcompanion.bot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_history")
public class QuizHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discordId;
    private Integer questionId;
    private String category;
    private Boolean isCorrect;
    private LocalDateTime answeredAt;

    public QuizHistory() {}

    public QuizHistory(String discordId, Integer questionId, String category, Boolean isCorrect) {
        this.discordId = discordId;
        this.questionId = questionId;
        this.category = category;
        this.isCorrect = isCorrect;
        this.answeredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getDiscordId() { return discordId; }
    public Integer getQuestionId() { return questionId; }
    public String getCategory() { return category; }
    public Boolean getIsCorrect() { return isCorrect; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
}
