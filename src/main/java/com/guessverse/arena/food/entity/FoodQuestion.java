package com.guessverse.arena.food.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String food;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    @Column(name = "image_name")
    private String imageName;

    @Column(nullable = false)
    private Integer level;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}