package com.guessverse.arena.flag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "flag_game_used_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagGameUsedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private Long questionId;

    @Builder.Default
    private LocalDateTime askedAt = LocalDateTime.now();
}