CREATE TABLE logo_questions
(
    id BIGSERIAL PRIMARY KEY,
    image_name VARCHAR(255) NOT NULL,
    answer VARCHAR(255) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);