CREATE TABLE flag_game_used_questions
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    session_id UUID NOT NULL,
    question_id BIGINT NOT NULL,

    asked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_flag_used_question_session
        FOREIGN KEY (session_id)
            REFERENCES flag_game_sessions(id)
            ON DELETE CASCADE
);
