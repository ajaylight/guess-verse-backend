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
     * Logo image URL or relative path
     * Example:
     * /logos/nike.png
     */
    @Column(nullable = false)
    private String imageName;;

    /**
     * Correct answer
     * Example:
     * NIKE
     */
    @Column(nullable = false)
    private String answer;

    /**
     * Optional alternative accepted answers
     * Example:
     * ["P&G","PROCTER AND GAMBLE"]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "logo_question_aliases",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "alias")
    private List<String> aliases;

    /**
     * Internal difficulty.
     * User never selects this.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    /**
     * Whether this question is active.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String info;
}