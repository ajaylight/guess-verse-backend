CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(30) NOT NULL UNIQUE,

    email VARCHAR(255) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    display_name VARCHAR(50),

    profile_picture_url VARCHAR(255),

    level INT NOT NULL DEFAULT 1,

    xp INT NOT NULL DEFAULT 0,

    coins INT NOT NULL DEFAULT 0,

    is_online BOOLEAN NOT NULL DEFAULT FALSE,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);