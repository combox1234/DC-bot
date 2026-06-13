package com.placementcompanion.bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitHubApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String GITHUB_API_BASE = "https://api.github.com/users/";
    private static final String GITHUB_REPO_BASE = "https://api.github.com/repos/";

    public GitHubApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> fetchProfile(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_BASE + username))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Placement-Companion-Bot")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> fetchRecentActivity(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_BASE + username + "/events/public"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Placement-Companion-Bot")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                List<Map<String, Object>> events = new ArrayList<>();
                if (root.isArray()) {
                    for (int i = 0; i < Math.min(root.size(), 5); i++) {
                        JsonNode node = root.get(i);
                        events.add(objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {}));
                    }
                }
                return events;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> fetchCommits(String owner, String repo) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_REPO_BASE + owner + "/" + repo + "/commits"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Placement-Companion-Bot")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                List<Map<String, Object>> commits = new ArrayList<>();
                if (root.isArray()) {
                    for (int i = 0; i < Math.min(root.size(), 10); i++) {
                        JsonNode node = root.get(i);
                        commits.add(objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {}));
                    }
                }
                return commits;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> fetchRepoContents(String owner, String repo, String path) {
        try {
            String url = GITHUB_REPO_BASE + owner + "/" + repo + "/contents";
            if (path != null && !path.isEmpty()) {
                url += "/" + path;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Placement-Companion-Bot")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                List<Map<String, Object>> contents = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode node : root) {
                        contents.add(objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {}));
                    }
                } else if (root.isObject()) {
                    // It's a single file response
                    contents.add(objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {}));
                }
                return contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> fetchFileContent(String owner, String repo, String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_REPO_BASE + owner + "/" + repo + "/contents/" + path))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Placement-Companion-Bot")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
