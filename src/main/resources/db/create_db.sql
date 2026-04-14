-- =========================
-- RESET (DROP ALL)
-- =========================

DROP TABLE IF EXISTS tb_equipped_cards CASCADE;
DROP TABLE IF EXISTS tb_card_instances CASCADE;
DROP TABLE IF EXISTS tb_card_definitions CASCADE;
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

                          active_character_instance_id INT
);

-- =========================
-- RANDOM MESSAGES
-- =========================

CREATE TABLE tb_random_messages (
                                    id SERIAL PRIMARY KEY,
                                    content TEXT NOT NULL
);

-- =========================
-- CARD DEFINITIONS
-- =========================

CREATE TABLE tb_card_definitions (
                                     id TEXT PRIMARY KEY,
                                     name TEXT NOT NULL,
                                     description TEXT NOT NULL,

                                     type TEXT NOT NULL,
                                     rarity TEXT NOT NULL,

                                     faction TEXT,
                                     tags TEXT NOT NULL DEFAULT '',

                                     base_stats TEXT NOT NULL,
                                     abilities TEXT NOT NULL DEFAULT '[]'
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
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_definition
                                       FOREIGN KEY (definition_id)
                                           REFERENCES tb_card_definitions(id)
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
-- FK CIRCULAR (ADICIONADA DEPOIS)
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
-- SEEDS
-- =========================

-- 🟢 SLIME (CHARACTER)
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'SLIME',
           'Slime',
           'Uma criatura gelatinosa básica.',
           'CHARACTER',
           'COMMON',
           'slime',
           'slime,starter',
           '{
               "HP": 1000,
               "ATK": 80,
               "DEF": 60,
               "CRIT_CHANCE": 0.05,
               "CRIT_DAMAGE": 1.5,
               "SPEED": 90
           }',
           '[
               {
                   "type": "HEAL",
                   "value": 5,
                   "trigger": "ON_TURN_START",
                   "target": "SELF"
               }
           ]'
       );

-- 🛡️ IRON ARMOR
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'IRON_ARMOR',
           'Iron Armor',
           'Armadura que reduz dano recebido.',
           'EQUIPMENT',
           'COMMON',
           NULL,
           'armor,defense',
           '{
               "HP": 100,
               "DEF": 60,
               "SPEED": -10
           }',
           '[
               {
                   "type": "INCOMING_DAMAGE_REDUCTION",
                   "value": 20,
                   "trigger": "ON_DAMAGE_TAKEN",
                   "target": "SELF"
               }
           ]'
       );

-- 🛡️ HEAVY IRON ARMOR
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'HEAVY_IRON_ARMOR',
           'Heavy Iron Armor',
           'Armadura pesada que reduz dano recebido.',
           'EQUIPMENT',
           'RARE',
           NULL,
           'armor,defense',
           '{
               "HP": 100,
               "DEF": 140,
               "SPEED": -40
           }',
           '[]'
       );

-- 🎰 GAMBLER CHARM
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'GAMBLER_CHARM',
           'Gambler Charm',
           'Um artefato caótico.',
           'EQUIPMENT',
           'EPIC',
           NULL,
           'rng,chaos',
           '{}',
           '[
               {
                   "type": "RNG_EFFECT",
                   "trigger": "ON_TURN_START",
                   "target": "SELF"
               }
           ]'
       );

-- 🧛 VAMPIRE CORE
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'VAMPIRE_CORE',
           'Vampire Core',
           'Rouba vida ao atacar.',
           'EQUIPMENT',
           'EPIC',
           'vampire',
           'vampire,lifesteal',
           '{}',
           '[
               {
                   "type": "LIFESTEAL",
                   "value": 25,
                   "trigger": "ON_HIT",
                   "target": "SELF"
               }
           ]'
       );

-- 🐟 CRITFISH
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'CRITFISH',
           'Critfish',
           'Um item raro que trava sua chance de crítico em 5% e multiplica o dano crítico em 3x.',
           'EQUIPMENT',
           'LEGENDARY',
           NULL,
           'critfish,critical',
           '{
               "CRIT_CHANCE": 0.05,
               "CRIT_DAMAGE": 3.0
           }',
           '[
               {
                   "type": "CRIT_PROFILE",
                   "trigger": "PASSIVE",
                   "target": "SELF",
                   "params": {
                       "critChance": "0.05",
                       "critDamage": "3.0"
                   }
               }
           ]'
       );

