

CREATE TABLE logo_question_aliases
(
    question_id BIGINT NOT NULL,

    alias VARCHAR(255) NOT NULL,

    CONSTRAINT fk_logo_alias_question
        FOREIGN KEY (question_id)
            REFERENCES logo_questions(id)
            ON DELETE CASCADE
);