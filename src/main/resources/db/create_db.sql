-- =========================
-- USERS
-- =========================

CREATE TABLE IF NOT EXISTS tb_users (
                                        id SERIAL PRIMARY KEY,
                                        discord_id BIGINT UNIQUE NOT NULL,

    -- 💰 moedas
                                        konos INT DEFAULT 0,
                                        essence INT DEFAULT 0

);

ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS riot_puuid TEXT;
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS riot_region TEXT;

ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS daily_reward_claimed_at TIMESTAMP;
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS daily_streak INT DEFAULT 0;

-- =========================
-- RANDOM MESSAGES
-- =========================

CREATE TABLE IF NOT EXISTS tb_random_messages (
                                                  id SERIAL PRIMARY KEY,
                                                  content TEXT NOT NULL
);

-- =========================
-- CARD DEFINITIONS
-- =========================

CREATE TABLE IF NOT EXISTS tb_card_definitions (
                                                   id TEXT PRIMARY KEY,
                                                   name TEXT NOT NULL,
                                                   type TEXT NOT NULL,         -- CHARACTER / EQUIPMENT
                                                   rarity TEXT NOT NULL,       -- COMMON / RARE / ...
                                                   base_stats TEXT NOT NULL,   -- JSON
                                                   effect_id TEXT
);

-- =========================
-- CARD INSTANCES (player inventory)
-- =========================

CREATE TABLE IF NOT EXISTS tb_card_instances (
                                                 id SERIAL PRIMARY KEY,
                                                 user_id INT NOT NULL,
                                                 definition_id TEXT NOT NULL,
                                                 level INT DEFAULT 1,
                                                 upgraded BOOLEAN DEFAULT FALSE,

                                                 CONSTRAINT fk_user
                                                     FOREIGN KEY (user_id)
                                                         REFERENCES tb_users(id)
                                                         ON DELETE CASCADE,

                                                 CONSTRAINT fk_definition
                                                     FOREIGN KEY (definition_id)
                                                         REFERENCES tb_card_definitions(id)
                                                         ON DELETE CASCADE
);

-- =========================
-- EQUIPPED CARDS
-- =========================

CREATE TABLE IF NOT EXISTS tb_equipped_cards (
                                                 id SERIAL PRIMARY KEY,
                                                 character_instance_id INT NOT NULL,
                                                 card_instance_id INT NOT NULL,
                                                 slot INT NOT NULL, -- 0,1,2

                                                 CONSTRAINT fk_character
                                                     FOREIGN KEY (character_instance_id)
                                                         REFERENCES tb_card_instances(id)
                                                         ON DELETE CASCADE,

                                                 CONSTRAINT fk_card
                                                     FOREIGN KEY (card_instance_id)
                                                         REFERENCES tb_card_instances(id)
                                                         ON DELETE CASCADE,

                                                 CONSTRAINT unique_slot UNIQUE (character_instance_id, slot)
);

-- =========================
-- INDEXES
-- =========================

CREATE INDEX IF NOT EXISTS idx_card_instances_user
    ON tb_card_instances(user_id);

CREATE INDEX IF NOT EXISTS idx_card_instances_definition
    ON tb_card_instances(definition_id);

-- =========================
-- SEEDS
-- =========================

-- 🟢 SLIME (personagem base)
INSERT INTO tb_card_definitions (id, name, type, rarity, base_stats, effect_id)
VALUES (
           'SLIME',
           'Slime',
           'CHARACTER',
           'COMMON',
           '{
               "HP": 1000,
               "ATK": 80,
               "DEF": 60,
               "CRIT_CHANCE": 0.05,
               "CRIT_DAMAGE": 1.5,
               "SPEED": 90
           }',
           NULL
       )
ON CONFLICT (id) DO NOTHING;

-- 🛡️ IRON ARMOR
INSERT INTO tb_card_definitions (id, name, type, rarity, base_stats, effect_id)
VALUES (
           'IRON_ARMOR',
           'Iron Armor',
           'EQUIPMENT',
           'COMMON',
           '{
               "HP": 100,
               "DEF": 60,
               "SPEED": -10
           }',
           NULL
       )
ON CONFLICT (id) DO NOTHING;

-- 🐟 CRITFISH
INSERT INTO tb_card_definitions (id, name, type, rarity, base_stats, effect_id)
VALUES (
           'CRITFISH',
           'Critfish',
           'EQUIPMENT',
           'LEGENDARY',
           '{}',
           'CRITFISH'
       )
ON CONFLICT (id) DO NOTHING;

ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS last_work_at TIMESTAMP;
