package com.guessverse.arena.flag.entity;

import com.guessverse.game.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flag_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    private String answer;

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