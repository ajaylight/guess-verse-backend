CREATE TABLE food_questions (
                                id BIGSERIAL PRIMARY KEY,
                                food VARCHAR(255) NOT NULL,
                                category VARCHAR(50) NOT NULL,
                                image_name VARCHAR(255),
                                level INTEGER NOT NULL,
                                active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE food_game_sessions (
                                    id UUID PRIMARY KEY,
                                    category VARCHAR(50) NOT NULL,
                                    level INTEGER NOT NULL,
                                    current_question INTEGER NOT NULL,
                                    total_score INTEGER NOT NULL DEFAULT 0,
                                    correct_answers INTEGER NOT NULL DEFAULT 0,
                                    completed BOOLEAN NOT NULL DEFAULT FALSE,
                                    current_question_started_at TIMESTAMP,
                                    created_at TIMESTAMP NOT NULL,
                                    completed_at TIMESTAMP
);

CREATE TABLE food_game_used_questions (
                                          id BIGSERIAL PRIMARY KEY,
                                          session_id UUID NOT NULL,
                                          question_id BIGINT NOT NULL,

                                          CONSTRAINT fk_food_used_session
                                              FOREIGN KEY (session_id)
                                                  REFERENCES food_game_sessions(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_food_used_question
                                              FOREIGN KEY (question_id)
                                                  REFERENCES food_questions(id),

                                          CONSTRAINT uk_food_session_question
                                              UNIQUE (session_id, question_id)
);

CREATE INDEX idx_food_questions_category_level
    ON food_questions(category, level);

CREATE INDEX idx_food_used_session
    ON food_game_used_questions(session_id);