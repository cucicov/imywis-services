package com.example.imywisservices.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileService {

    private final SupabaseClient supabaseClient;

    public UserProfileService(SupabaseClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public boolean userExists(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        if (!supabaseClient.isConfigured()) {
            // If Supabase is not configured, allow all users (for development)
            return true;
        }

        try {
            // Only select user_id to minimize data transfer
            String filter = "select=user_id&user_id=eq." + userId.trim();
            List<UserProfile> profiles = supabaseClient
                    .get("user_profiles", filter, UserProfileList.class)
                    .block();

            System.out.println("Checking user_id: " + userId.trim());
            System.out.println("Query filter: " + filter);
            System.out.println("Results found: " + (profiles != null ? profiles.size() : 0));
            if (profiles != null && !profiles.isEmpty()) {
                System.out.println("User profile found: " + profiles.get(0).getUserId());
            }

            return profiles != null && !profiles.isEmpty();
        } catch (Exception e) {
            // Log error and deny access on failure
            System.err.println("Error checking user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfile {
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("handle")
        private String handle;

        @JsonProperty("data")
        private Object data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfileList extends java.util.ArrayList<UserProfile> {
    }
}
