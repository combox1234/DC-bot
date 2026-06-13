package com.placementcompanion.bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlacementDailyService {

    private List<DailyChallengeNode> allChallenges = new ArrayList<>();

    @PostConstruct
    public void loadChallenges() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("daily_challenges.json").getInputStream();
            allChallenges = mapper.readValue(is, new TypeReference<List<DailyChallengeNode>>() {});
            System.out.println("Loaded " + allChallenges.size() + " placement daily challenges.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DailyChallengeNode getChallengeForToday() {
        String today = LocalDate.now().toString();
        // Fallback to a random challenge if today's isn't found
        DailyChallengeNode fallback = allChallenges.isEmpty() ? null : allChallenges.get(0);
        for (DailyChallengeNode challenge : allChallenges) {
            if (today.equals(challenge.getDate())) {
                return challenge;
            }
        }
        return fallback; // If specific date not found, just return the first one
    }

    public static class DailyChallengeNode {
        private String id;
        private String date;
        private String title;
        private String description;
        private String difficulty;
        private String category;
        private int xp_reward;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getXp_reward() { return xp_reward; }
        public void setXp_reward(int xp_reward) { this.xp_reward = xp_reward; }
    }
}
