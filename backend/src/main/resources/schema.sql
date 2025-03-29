DROP TABLE IF EXISTS _user;
DROP TABLE IF EXISTS refresh_token;

CREATE TABLE _user (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(255),
    token_version INTEGER NOT NULL DEFAULT 0,
    activation_token VARCHAR(255) UNIQUE,
    activation_token_expires_at TIMESTAMP,
    password_setup_token VARCHAR(255) UNIQUE,
    password_setup_token_expires_at TIMESTAMP,
    pending_password_setup BOOLEAN NOT NULL DEFAULT FALSE,
    activated BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE refresh_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255),
    is_logged_out BOOLEAN,
    user_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES _user(id)
);
