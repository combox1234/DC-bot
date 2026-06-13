package com.placementcompanion.bot.model;

import java.util.List;

public class Question {
    private int id;
    private String category;
    private String question;
    private List<String> options;
    private int answer;
    private String explanation;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getAnswer() { return answer; }
    public void setAnswer(int answer) { this.answer = answer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
