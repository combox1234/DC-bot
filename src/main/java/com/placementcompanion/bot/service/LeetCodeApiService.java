package com.placementcompanion.bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.time.Duration;

@Service
public class LeetCodeApiService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, String> getDailyChallenge() {
        String query = "query questionOfToday { activeDailyCodingChallengeQuestion { date link question { title titleSlug difficulty } } }";
        
        try {
            String requestBody = mapper.writeValueAsString(Map.of("query", query));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Referer", "https://leetcode.com")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            
            JsonNode challenge = root.path("data").path("activeDailyCodingChallengeQuestion");
            if (challenge.isMissingNode() || challenge.isNull()) {
                return null;
            }

            JsonNode q = challenge.path("question");
            Map<String, String> result = new HashMap<>();
            result.put("title", q.path("title").asText());
            result.put("titleSlug", q.path("titleSlug").asText());
            result.put("difficulty", q.path("difficulty").asText());
            result.put("link", "https://leetcode.com" + challenge.path("link").asText());
            result.put("date", challenge.path("date").asText());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> fetchUserProfile(String username) {
        String query = "query getUserProfile($username: String!) { matchedUser(username: $username) { submitStats { acSubmissionNum { difficulty count } } profile { ranking } } userContestRanking(username: $username) { rating globalRanking } }";
        try {
            String requestBody = mapper.writeValueAsString(Map.of("query", query, "variables", Map.of("username", username)));
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://leetcode.com/graphql")).timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json").header("Referer", "https://leetcode.com").header("User-Agent", "Mozilla/5.0").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            JsonNode matched = root.path("data").path("matchedUser");
            if (matched.isMissingNode() || matched.isNull()) return null;

            JsonNode stats = matched.path("submitStats").path("acSubmissionNum");
            Map<String, Integer> solved = new HashMap<>();
            solved.put("All", 0); solved.put("Easy", 0); solved.put("Medium", 0); solved.put("Hard", 0);
            for (JsonNode node : stats) {
                solved.put(node.path("difficulty").asText(), node.path("count").asInt());
            }

            JsonNode profile = matched.path("profile");
            JsonNode contest = root.path("data").path("userContestRanking");

            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("totalSolved", solved.get("All"));
            result.put("easySolved", solved.get("Easy"));
            result.put("mediumSolved", solved.get("Medium"));
            result.put("hardSolved", solved.get("Hard"));
            result.put("ranking", profile.path("ranking").asInt(0));
            result.put("contestRating", contest.isMissingNode() || contest.isNull() ? null : Math.round(contest.path("rating").asDouble(0) * 10.0) / 10.0);
            result.put("globalRanking", contest.isMissingNode() || contest.isNull() ? null : contest.path("globalRanking").asInt());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Map<String, Object>> fetchContests() {
        String query = "query { allContests { title startTime duration titleSlug } }";
        try {
            String requestBody = mapper.writeValueAsString(Map.of("query", query));
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://leetcode.com/graphql")).timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json").header("Referer", "https://leetcode.com").header("User-Agent", "Mozilla/5.0").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            JsonNode contestsNode = root.path("data").path("allContests");
            if (contestsNode.isMissingNode() || contestsNode.isNull()) return null;

            List<Map<String, Object>> contests = new java.util.ArrayList<>();
            for (JsonNode node : contestsNode) {
                Map<String, Object> c = new HashMap<>();
                c.put("title", node.path("title").asText());
                c.put("startTime", node.path("startTime").asLong());
                c.put("duration", node.path("duration").asDouble());
                c.put("titleSlug", node.path("titleSlug").asText());
                contests.add(c);
            }
            return contests;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> validateProblemSlug(String slug) {
        String query = "query getQuestion($titleSlug: String!) { question(titleSlug: $titleSlug) { title difficulty } }";
        try {
            String requestBody = mapper.writeValueAsString(Map.of("query", query, "variables", Map.of("titleSlug", slug)));
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://leetcode.com/graphql")).timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json").header("Referer", "https://leetcode.com").header("User-Agent", "Mozilla/5.0").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            JsonNode questionNode = root.path("data").path("question");
            if (questionNode.isMissingNode() || questionNode.isNull()) return null;

            Map<String, String> result = new HashMap<>();
            result.put("title", questionNode.path("title").asText("Unknown"));
            result.put("difficulty", questionNode.path("difficulty").asText("Unknown"));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
