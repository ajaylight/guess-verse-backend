package com.guessverse.arena.logo.entity;

import com.guessverse.game.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "logo_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Logo.dev company domain used internally by the backend.
     * Example: nike.com
     *
     * This field is never exposed in the normal question DTO.
     */
    @Column(name = "image_domain", nullable = false)
    private String imageDomain;

    @Column(nullable = false)
    private String answer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "logo_question_aliases",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "alias")
    private List<String> aliases;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;


    @Column(nullable = false)
    private Integer level;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String info;
}