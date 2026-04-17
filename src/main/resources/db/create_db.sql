-- =========================
-- RESET (DROP ALL)
-- =========================

DROP TABLE IF EXISTS tb_equipped_cards CASCADE;
DROP TABLE IF EXISTS tb_card_instances CASCADE;
DROP TABLE IF EXISTS tb_random_messages CASCADE;
DROP TABLE IF EXISTS tb_users CASCADE;

-- =========================
-- USERS
-- =========================

CREATE TABLE tb_users (
    id SERIAL PRIMARY KEY,
    discord_id BIGINT UNIQUE NOT NULL,

    konos INT DEFAULT 0,
    essence INT DEFAULT 0,

    riot_puuid TEXT,
    riot_region TEXT,

    daily_reward_claimed_at TIMESTAMP,
    daily_streak INT DEFAULT 0,

    last_work_at TIMESTAMP,

    active_character_instance_id INT,

    -- gacha pity counters
    pity_epic INT DEFAULT 0,
    pity_legendary INT DEFAULT 0
);

-- =========================
-- RANDOM MESSAGES
-- =========================

CREATE TABLE tb_random_messages (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL
);

-- =========================
-- CARD INSTANCES
-- =========================

CREATE TABLE tb_card_instances (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    definition_id TEXT NOT NULL,

    level INT DEFAULT 1,
    upgraded BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES tb_users(id)
        ON DELETE CASCADE
);

-- =========================
-- EQUIPPED CARDS
-- =========================

CREATE TABLE tb_equipped_cards (
    id SERIAL PRIMARY KEY,
    character_instance_id INT NOT NULL,
    card_instance_id INT NOT NULL,
    slot INT NOT NULL,

    CONSTRAINT fk_character
        FOREIGN KEY (character_instance_id)
        REFERENCES tb_card_instances(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_card
        FOREIGN KEY (card_instance_id)
        REFERENCES tb_card_instances(id)
        ON DELETE CASCADE,

    CONSTRAINT unique_slot UNIQUE (character_instance_id, slot),
    CONSTRAINT unique_card_instance UNIQUE (card_instance_id)
);

-- =========================
-- FK CIRCULAR (ADDED AFTER)
-- =========================

ALTER TABLE tb_users
    ADD CONSTRAINT fk_active_character
        FOREIGN KEY (active_character_instance_id)
        REFERENCES tb_card_instances(id)
        ON DELETE SET NULL;

-- =========================
-- INDEXES
-- =========================

CREATE INDEX idx_card_instances_user
    ON tb_card_instances(user_id);

CREATE INDEX idx_card_instances_definition
    ON tb_card_instances(definition_id);

CREATE INDEX idx_equipped_character
    ON tb_equipped_cards(character_instance_id);

