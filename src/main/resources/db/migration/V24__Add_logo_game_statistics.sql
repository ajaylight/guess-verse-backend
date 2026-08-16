ALTER TABLE logo_game_sessions
    ADD COLUMN total_info_hints INTEGER NOT NULL DEFAULT 0;

ALTER TABLE logo_game_sessions
    ADD COLUMN total_letters_revealed INTEGER NOT NULL DEFAULT 0;

ALTER TABLE logo_game_sessions
    ADD COLUMN total_answers_revealed INTEGER NOT NULL DEFAULT 0;