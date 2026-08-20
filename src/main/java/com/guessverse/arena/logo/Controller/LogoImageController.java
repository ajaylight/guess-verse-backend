package com.guessverse.arena.logo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guessverse.arena.logo.entity.LogoQuestion;
import com.guessverse.arena.logo.repository.LogoQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/logo/image")
@RequiredArgsConstructor
public class LogoImageController {

    private final LogoQuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Value("${logo.dev.publishable-key:}")
    private String logoDevPublishableKey;

    /*
     * Optional secret key used only on the backend to ask Logo.dev for
     * the brandmark (symbol/icon) instead of the full wordmark/logo.
     *
     * Keep this in an environment variable. Never send it to the frontend.
     */
    @Value("${logo.dev.secret-key:}")
    private String logoDevSecretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getLogoImage(
            @PathVariable String imageId
    ) {
        if (!imageId.startsWith("logo-")) {
            return ResponseEntity.notFound().build();
        }

        final Long questionId;
        try {
            questionId = Long.parseLong(imageId.substring("logo-".length()));
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }

        LogoQuestion question = questionRepository.findById(questionId).orElse(null);

        if (question == null || question.getImageDomain() == null
                || question.getImageDomain().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        if (logoDevPublishableKey == null || logoDevPublishableKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        String domain = question.getImageDomain().trim();
        String logoUrl = buildLogoUrl(domain);

        try {
            /*
             * If the secret key is configured, prefer Logo.dev's
             * Brand API brandmark. A brandmark is the symbol-only
             * asset and avoids showing answers such as "Uber" directly
             * inside the quiz image when a standalone mark exists.
             */
            if (logoDevSecretKey != null && !logoDevSecretKey.isBlank()) {
                String brandmarkUrl = resolveBrandmarkUrl(domain);

                if (brandmarkUrl != null && !brandmarkUrl.isBlank()) {
                    logoUrl = addImageParameters(brandmarkUrl);
                }
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(logoUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            MediaType mediaType = response.headers()
                    .firstValue("content-type")
                    .map(value -> {
                        try {
                            return MediaType.parseMediaType(value);
                        } catch (IllegalArgumentException ignored) {
                            return MediaType.IMAGE_PNG;
                        }
                    })
                    .orElse(MediaType.IMAGE_PNG);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    private String buildLogoUrl(String domain) {
        return "https://img.logo.dev/"
                + domain
                + "?token=" + logoDevPublishableKey
                + "&size=512&format=png&retina=true&fallback=monogram";
    }

    /**
     * Ask Logo.dev Brand API for the standalone brandmark URL.
     * This endpoint requires the server-side secret key.
     */
    private String resolveBrandmarkUrl(String domain)
            throws IOException, InterruptedException {

        String brandApiUrl = "https://api.logo.dev/brand/" + domain;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(brandApiUrl))
                .timeout(Duration.ofSeconds(10))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + logoDevSecretKey)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode brandmark = root.get("brandmark");

        if (brandmark == null || brandmark.isNull()) {
            return null;
        }

        String url = brandmark.asText(null);
        if (url == null || url.isBlank()) {
            return null;
        }

        /*
         * Logo.dev normally returns a ready-to-use URL. If the returned
         * URL does not contain a token, append our publishable key.
         */
        if (!url.contains("token=")) {
            url += (url.contains("?") ? "&" : "?")
                    + "token=" + logoDevPublishableKey;
        }

        return url;
    }

    private String addImageParameters(String url) {
        String separator = url.contains("?") ? "&" : "?";

        return url
                + separator
                + "size=512&format=png&retina=true";
    }
}
