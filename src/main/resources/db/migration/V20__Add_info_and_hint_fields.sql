ALTER TABLE logo_questions
    ADD COLUMN info TEXT;

ALTER TABLE logo_game_sessions
    ADD COLUMN current_question_letter_hints INT DEFAULT 0;

ALTER TABLE logo_game_sessions
    ADD COLUMN answer_revealed BOOLEAN DEFAULT FALSE;

ALTER TABLE logo_game_sessions
    ADD COLUMN revealed_positions TEXT;