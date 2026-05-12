package com.example.imywisservices.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SupabaseClient {

    private final WebClient webClient;

    public SupabaseClient(@Value("${supabase.url}") String supabaseUrl,
                          @Value("${supabase.key}") String supabaseKey) {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            this.webClient = null;
            return;
        }

        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .build();
    }

    public <T> Mono<T> get(String table, String filter, Class<T> responseType) {
        if (webClient == null) {
            return Mono.empty();
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + table)
                        .query(filter)
                        .build())
                .retrieve()
                .bodyToMono(responseType);
    }

    public boolean isConfigured() {
        return webClient != null;
    }
}
