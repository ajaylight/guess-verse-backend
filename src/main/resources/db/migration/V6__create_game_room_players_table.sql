CREATE TABLE game_room_players
(
    id BIGSERIAL PRIMARY KEY,

    game_room_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    score INT NOT NULL DEFAULT 0,

    eliminated BOOLEAN NOT NULL DEFAULT FALSE,

    joined_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_grp_room
        FOREIGN KEY (game_room_id)
            REFERENCES game_rooms(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_grp_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_room_user
        UNIQUE (game_room_id, user_id)
);