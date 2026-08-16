package com.guessverse.arena.base;

import com.guessverse.game.enums.Difficulty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseQuestion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private boolean active = true;
}