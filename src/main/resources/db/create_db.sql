CREATE TABLE IF NOT EXISTS tb_users (
    id SERIAL PRIMARY KEY,
    discord_id BIGINT UNIQUE NOT NULL,
    money INT DEFAULT 0
);