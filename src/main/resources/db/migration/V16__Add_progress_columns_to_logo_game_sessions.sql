ALTER TABLE logo_game_sessions
ADD COLUMN current_question INTEGER DEFAULT 1;

ALTER TABLE logo_game_sessions
ADD COLUMN correct_answers INTEGER DEFAULT 0;

ALTER TABLE logo_game_sessions
ADD COLUMN total_questions INTEGER DEFAULT 10;