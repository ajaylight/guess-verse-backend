ALTER TABLE logo_game_sessions
    ADD COLUMN awaiting_continue BOOLEAN NOT NULL DEFAULT FALSE;