package com.guessverse.arena.flag.controller;

import com.guessverse.arena.flag.entity.FlagQuestion;
import com.guessverse.arena.flag.repository.FlagQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flag/image")
@RequiredArgsConstructor
public class FlagImageController {

    private final FlagQuestionRepository questionRepository;

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String imageId
    ) {

        if (!imageId.startsWith("flag-")) {
            return ResponseEntity.badRequest().build();
        }

        String idValue =
                imageId.substring("flag-".length());

        Long questionId;

        try {
            questionId = Long.parseLong(idValue);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        FlagQuestion question =
                questionRepository.findById(questionId)
                        .orElse(null);

        if (question == null ||
                question.getImageName() == null ||
                question.getImageName().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        Resource resource =
                new ClassPathResource(
                        "flag-images/" +
                                question.getImageName()
                );

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}