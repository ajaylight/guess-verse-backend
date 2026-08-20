ALTER TABLE food_game_sessions
ADD COLUMN current_question_id BIGINT;

ALTER TABLE food_game_sessions
ADD CONSTRAINT fk_food_current_question
FOREIGN KEY (current_question_id)
REFERENCES food_questions(id);