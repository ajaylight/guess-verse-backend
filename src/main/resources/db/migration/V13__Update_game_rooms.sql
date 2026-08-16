DROP TABLE IF EXISTS game_rooms CASCADE;

CREATE TABLE game_rooms
(
    id BIGSERIAL PRIMARY KEY,

    room_code VARCHAR(8) NOT NULL UNIQUE,

    host_id BIGINT NOT NULL,

    arena_type VARCHAR(30) NOT NULL,

    difficulty VARCHAR(20) NOT NULL,

    max_players INT NOT NULL,

    current_players INT NOT NULL DEFAULT 1,

    game_state VARCHAR(20) NOT NULL DEFAULT 'WAITING',

    finished BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_game_room_host
        FOREIGN KEY (host_id)
            REFERENCES users(id)
);


DROP TABLE IF EXISTS game_modes CASCADE;