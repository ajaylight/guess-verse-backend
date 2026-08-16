package com.guessverse.arena.logo.controller;

import com.guessverse.arena.logo.entity.LogoQuestion;
import com.guessverse.arena.logo.repository.LogoQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/logo/image")
@RequiredArgsConstructor
public class LogoImageController {

    private final LogoQuestionRepository questionRepository;

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> getLogoImage(
            @PathVariable String imageId
    ) throws IOException {

        if (!imageId.startsWith("logo-")) {
            return ResponseEntity.notFound().build();
        }

        String idPart = imageId.substring("logo-".length());

        final Long questionId;

        try {
            questionId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }

        LogoQuestion question = questionRepository
                .findById(questionId)
                .orElse(null);

        if (question == null || question.getImageName() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new ClassPathResource(
                "logo-images/" + question.getImageName()
        );

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType =
                MediaTypeFactory
                        .getMediaType(resource.getFilename())
                        .orElse(MediaType.IMAGE_PNG);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}