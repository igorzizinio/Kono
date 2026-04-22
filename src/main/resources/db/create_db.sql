-- =========================
-- RESET (DROP ALL)
-- =========================

DROP TABLE IF EXISTS tb_equipped_cards CASCADE;
DROP TABLE IF EXISTS tb_card_instances CASCADE;
DROP TABLE IF EXISTS tb_battle_victories CASCADE;
DROP TABLE IF EXISTS tb_battle_team_slots CASCADE;
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

-- =========================
-- BATTLE TEAM SLOTS
-- =========================

CREATE TABLE tb_battle_team_slots (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    slot INT NOT NULL,
    character_instance_id INT NOT NULL,

    CONSTRAINT fk_battle_team_user
        FOREIGN KEY (user_id)
        REFERENCES tb_users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_battle_team_character
        FOREIGN KEY (character_instance_id)
        REFERENCES tb_card_instances(id)
        ON DELETE CASCADE,

    CONSTRAINT unique_team_slot UNIQUE (user_id, slot),
    CONSTRAINT unique_team_character UNIQUE (character_instance_id)
);

CREATE INDEX idx_battle_team_user
    ON tb_battle_team_slots(user_id);

-- =========================
-- BATTLE VICTORIES
-- =========================

CREATE TABLE tb_battle_victories (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    enemy_id TEXT NOT NULL,
    essence_reward INT NOT NULL DEFAULT 0,
    first_won_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_battle_victory_user
        FOREIGN KEY (user_id)
        REFERENCES tb_users(id)
        ON DELETE CASCADE,

    CONSTRAINT unique_user_enemy_victory UNIQUE (user_id, enemy_id)
);

CREATE INDEX idx_battle_victory_user
    ON tb_battle_victories(user_id);


ALTER TABLE tb_users
    ADD COLUMN smithing_stones INT DEFAULT 0;

ALTER TABLE tb_users
    ALTER COLUMN konos TYPE BIGINT;
