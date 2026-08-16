package com.guessverse.arena.flag.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flag/image")
public class FlagImageController {

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String imageId
    ) {

        if (!imageId.startsWith("flag-")) {
            return ResponseEntity.badRequest().build();
        }

        String id = imageId.substring("flag-".length());

        Resource resource =
                new ClassPathResource(
                        "flag-images/" + id + ".png"
                );

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}