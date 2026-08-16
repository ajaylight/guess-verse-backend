CREATE TABLE game_rooms
(
    id BIGSERIAL PRIMARY KEY,

    room_code VARCHAR(8) NOT NULL UNIQUE,

    host_id BIGINT NOT NULL,

    game_mode_id BIGINT NOT NULL,

    current_players INT NOT NULL,

    started BOOLEAN NOT NULL,

    finished BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_game_room_host
        FOREIGN KEY (host_id)
            REFERENCES users(id),

    CONSTRAINT fk_game_room_mode
        FOREIGN KEY (game_mode_id)
            REFERENCES game_modes(id)
);