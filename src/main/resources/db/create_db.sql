CREATE TABLE IF NOT EXISTS tb_users (
    id SERIAL PRIMARY KEY,
    discord_id BIGINT UNIQUE NOT NULL,
    money INT DEFAULT 0
);

ALTER TABLE tb_users ADD COLUMN riot_puuid TEXT;
ALTER TABLE tb_users ADD COLUMN riot_region TEXT;


CREATE TABLE IF NOT EXISTS tb_random_messages (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL
);


ALTER TABLE tb_users ADD COLUMN daily_reward_claimed_at TIMESTAMP;
ALTER TABLE tb_users ADD COLUMN daily_streak INT;
