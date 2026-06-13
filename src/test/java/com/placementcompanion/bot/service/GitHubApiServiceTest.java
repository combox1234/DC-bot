package com.placementcompanion.bot.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class GitHubApiServiceTest {

    @Test
    public void testFetchProfile() {
        GitHubApiService service = new GitHubApiService();
        // Use a known stable public profile, e.g., "torvalds" or "octocat"
        Map<String, Object> profile = service.fetchProfile("octocat");
        
        assertNotNull(profile, "Profile should not be null");
        assertEquals("octocat", profile.get("login"));
        assertNotNull(profile.get("public_repos"));
        assertNotNull(profile.get("followers"));
    }

    @Test
    public void testFetchRecentActivity() {
        GitHubApiService service = new GitHubApiService();
        List<Map<String, Object>> activity = service.fetchRecentActivity("octocat");
        
        // Activity can be empty if the user hasn't done anything recently,
        // but the list itself shouldn't be null if the API call succeeded.
        assertNotNull(activity, "Activity list should not be null");
    }

    @Test
    public void testFetchProfileInvalidUser() {
        GitHubApiService service = new GitHubApiService();
        Map<String, Object> profile = service.fetchProfile("thisuserdoesnotexist_1234567890_test");
        
        assertNull(profile, "Profile should be null for invalid user");
    }
}
