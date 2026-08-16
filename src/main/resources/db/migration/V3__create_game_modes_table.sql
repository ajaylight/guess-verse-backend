CREATE TABLE game_modes
(
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    round_time_seconds INT NOT NULL,

    max_players INT NOT NULL,

    hints_enabled BOOLEAN NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE
);