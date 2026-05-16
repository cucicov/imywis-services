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

    public String getMainPageHandle() {
        if (!supabaseClient.isConfigured()) {
            return null;
        }

        try {
            String filter = "select=handle&main_page=eq.true&limit=1";
            List<UserProfile> profiles = supabaseClient
                    .get("user_profiles", filter, UserProfileList.class)
                    .block();

            if (profiles != null && !profiles.isEmpty()) {
                return profiles.get(0).getHandle();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching main page handle: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean shouldShowAboutPopup(String handle) {
        if (handle == null || handle.isBlank()) {
            return false;
        }

        if (!supabaseClient.isConfigured()) {
            // If Supabase is not configured, show popup by default (for development)
            return true;
        }

        try {
            String filter = "select=about_popup&handle=eq." + handle.trim();
            List<UserProfile> profiles = supabaseClient
                    .get("user_profiles", filter, UserProfileList.class)
                    .block();

            if (profiles != null && !profiles.isEmpty()) {
                Boolean aboutPopup = profiles.get(0).getAboutPopup();
                // If about_popup is null or true, show the popup. Only hide if explicitly false.
                return aboutPopup == null || aboutPopup;
            }
            // If user not found, don't show popup
            return false;
        } catch (Exception e) {
            System.err.println("Error fetching about_popup for handle " + handle + ": " + e.getMessage());
            e.printStackTrace();
            // On error, don't show popup to be safe
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

        @JsonProperty("about_popup")
        private Boolean aboutPopup;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfileList extends java.util.ArrayList<UserProfile> {
    }
}
