CREATE TABLE flag_player_progress
(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    highest_unlocked_level INT NOT NULL DEFAULT 1,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_flag_progress_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);
