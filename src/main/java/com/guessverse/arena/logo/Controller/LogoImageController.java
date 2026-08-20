package com.guessverse.arena.logo.controller;

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

    @Value("${logo.dev.publishable-key:}")
    private String logoDevPublishableKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getLogoImage(
            @PathVariable String imageId
    ) throws InterruptedException {

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

        String logoUrl = "https://img.logo.dev/"
                + question.getImageDomain()
                + "?token=" + logoDevPublishableKey
                + "&size=512&format=png&retina=true&fallback=monogram";

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
    }
}