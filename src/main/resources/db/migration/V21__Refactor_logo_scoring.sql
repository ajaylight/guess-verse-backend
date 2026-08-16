ALTER TABLE logo_game_sessions
    ADD COLUMN total_score INTEGER NOT NULL DEFAULT 0;

ALTER TABLE logo_game_sessions
    ADD COLUMN current_question_reward INTEGER NOT NULL DEFAULT 100;

-- Old score column is no longer used.
ALTER TABLE logo_game_sessions
DROP COLUMN IF EXISTS score;