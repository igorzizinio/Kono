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
           'Uma criatura gelatinosa básica. Muita durabilidade, porém pouco dano. Sofre ferimentos ao atacar',
           'CHARACTER',
           'COMMON',
           'slime',
           'slime,starter',
           '{
                "HP": 580,
                "ATK": 38,
                "DEF": 8,
                "CRIT_CHANCE": 0.05,
                "CRIT_DAMAGE": 1.25,
                "SPEED": 80
           }',
           '[
                {
                    "type": "DAMAGE",
                    "value": 12,
                    "trigger": "ON_ATTACK",
                    "target": "SELF",
                    "params": {
                        "trueDamage": false,
                        "canCrit": false,
                        "canBeDodged": true,
                        "everyHits": 1
                    }
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
               "HP": 180,
               "DEF": 15,
               "SPEED": -5
           }',
           '[]'
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
           'heavy,armor,defense',
           '{
               "HP": 110,
               "DEF": 60,
               "SPEED": -20
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
           'gambler,chaos',
           '{}',
           '[
               {
                   "type": "RNG_EFFECT",
                   "trigger": "ON_TURN_START",
                    "target": "SELF",
                    "params": {
                        "profile": "DEFAULT"
                    }
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
                   "value": 10,
                   "trigger": "ON_HIT",
                    "target": "SELF",
                    "params": {
                        "everyHits": 1
                    }
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
                       "critChance": 0.05,
                       "critDamage": 3.0
                   }
               }
           ]'
       );


-- ??? undefined
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'UNDEFINED',
           'undefined',
           '`error: undefined is not a item`',
           'EQUIPMENT',
           'MYTHIC',
           NULL,
           'bug,chaos',
           '{}',
           '[
               {
                   "type": "RNG_EFFECT",
                   "trigger": "ON_TURN_START",
                   "target": "SELF",
                   "params": {
                       "profile": "UNDEFINED_BUG",
                       "selfDamageMin": 60,
                       "selfDamageMax": 140,
                       "enemyDamageMin": 70,
                       "enemyDamageMax": 160,
                       "selfHealMin": 50,
                       "selfHealMax": 120,
                       "enemyHealMin": 50,
                       "enemyHealMax": 120
                   }
               }
           ]'
       );


-- 🟢 Cavaleirinho (CHARACTER)
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'JUNIOR_KNIGHT',
           'Cavaleirinho',
           'Um jovem que sonha em ser um grande cavaleiro, ainda é pequeno e fraco. Porém ja possui sua propria armadura e espada de madeira.',
           'CHARACTER',
           'COMMON',
           NULL,
           'starter',
           '{
                "HP": 480,
                "ATK": 32,
                "DEF": 40,
                "CRIT_CHANCE": 0.05,
                "CRIT_DAMAGE": 1.25,
                "SPEED": 70
           }',
           '[]'
       );

-- 🔪 Bandido (CHARACTER)
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'THIEF',
           'Bandido',
           'Um jovem que cresceu nas ruas e aprendeu a sobreviver roubando. Apesar e ágil, ele ainda é fraco e tem muito a aprender.\n\n**Injusto:** Causa 10 de dano adicional ao acertar um ataque.',
           'CHARACTER',
           'RARE',
           NULL,
           'starter',
           '{
                "HP": 420,
                "ATK": 36,
                "DEF": 14,
                "CRIT_CHANCE": 0.10,
                "CRIT_DAMAGE": 1.4,
                "SPEED": 115
           }',
           '[
                {
                    "type": "DAMAGE",
                    "value": 10,
                    "trigger": "ON_HIT",
                    "target": "ENEMY",
                    "params": {
                        "trueDamage": false,
                        "canCrit": false,
                        "canBeDodged": false,
                        "everyHits": 1
                    }
                }
           ]'
       );


-- 🎰 Markus, Mestre das Apostas (CHARACTER)
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'MARKUS',
           'Markus, Mestre das Apostas',
           'Dono de um cassino onde fortunas são feitas e vidas são perdidas, Markus transforma cada turno em uma aposta. Quanto maior o risco e quanto pior a situacao, maiores podem ser as recompensas.',
           'CHARACTER',
           'LEGENDARY',
           'gambler',
           'rng,gambler,risk,chaos,scaling,boss',
           '{
                "HP": 720,
                "ATK": 58,
                "DEF": 14,
                "CRIT_CHANCE": 0.10,
                "CRIT_DAMAGE": 1.5,
                "SPEED": 110
           }',
           '[
                {
                    "type": "RNG_EFFECT",
                    "trigger": "ON_TURN_START",
                    "target": "SELF",
                    "params": {
                        "profile": "MARKUS_GAMBLER",
                        "strongEveryTurns": 5,
                        "coinBiasPerCoin": 0.45,
                        "strongMultiplier": 1.7,
                        "extraRollSpeedFactor": 0.002,
                        "enemyDamageMin": 24,
                        "enemyDamageMax": 44,
                        "selfHealMin": 18,
                        "selfHealMax": 36,
                        "selfDamageMin": 16,
                        "selfDamageMax": 34,
                        "atkBuffMin": 3,
                        "atkBuffMax": 7,
                        "speedBuffMin": 3,
                        "speedBuffMax": 8,
                        "shieldMin": 1,
                        "shieldMax": 1
                    }
                }
           ]'
       );


-- 🏹 Veyn, Besteiro de Markus (CHARACTER)
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'VEYN',
           'Veyn, Besteiro de Markus',
           'Atirador de elite do cassino de Markus. Nao aposta em forca bruta: aposta em ritmo, precisao e pressao constante. Quanto mais rapido ele joga, mais a sorte trabalha a favor.',
           'CHARACTER',
           'EPIC',
           'gambler',
           'rng,gambler,speed,archer,marksman,risk',
           '{
                "HP": 510,
                "ATK": 42,
                "DEF": 16,
                "CRIT_CHANCE": 0.10,
                "CRIT_DAMAGE": 1.25,
                "SPEED": 135
           }',
           '[
                {
                    "type": "DAMAGE",
                    "value": 12,
                    "trigger": "ON_HIT",
                    "target": "ENEMY",
                    "params": {
                        "trueDamage": false,
                        "canCrit": false,
                        "canBeDodged": true,
                        "everyHits": 2
                    }
                },
                {
                    "type": "RNG_EFFECT",
                    "trigger": "ON_TURN_START",
                    "target": "SELF",
                    "params": {
                        "profile": "MARKUS_GAMBLER",
                        "strongEveryTurns": 6,
                        "coinBiasPerCoin": 0.25,
                        "strongMultiplier": 1.4,
                        "extraRollSpeedFactor": 0.0028,
                        "enemyDamageMin": 18,
                        "enemyDamageMax": 34,
                        "selfHealMin": 4,
                        "selfHealMax": 16,
                        "selfDamageMin": 12,
                        "selfDamageMax": 24,
                        "atkBuffMin": 2,
                        "atkBuffMax": 6,
                        "speedBuffMin": 4,
                        "speedBuffMax": 8,
                        "shieldMin": 1,
                        "shieldMax": 1
                    }
                }
           ]'
       );


-- THE Statstick
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'STATSTICK',
           'O Statstick',
           'Dizem que todos já o usaram… ou estiveram à sua procura. Um simples graveto, escolhido não por seu poder oculto, mas pelos números que carrega.\nAumenta ATK, DEF e HP',
           'EQUIPMENT',
           'LEGENDARY',
           NULL,
           'stat',
           '{
                "HP": 240,
                "ATK": 60,
                "DEF": 25
           }',
           '[]'
       );


-- Wooden sword
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'WOODEN_SWORD',
           'Espada de Madeira',
           'Uma espada de madeira usada por recrutas iniciantes. Não é grande coisa, mas já é melhor que um graveto.',
           'EQUIPMENT',
           'COMMON',
           NULL,
           'starter',
           '{
                "ATK": 15
           }',
           '[]'
       );


-- Besta da Caçadora de Demônios
INSERT INTO tb_card_definitions (
    id, name, description, type, rarity,
    faction, tags, base_stats, abilities
)
VALUES (
           'DEMON_HUNTER_CROSSBOW',
           'Besta da Caçadora de Demônios',
           'Projetada para caçar o que não deveria morrer. Uma besta herdada de uma antiga guerreira de um reino esquecido, com flechas embebidas em prata pura. A cada três ataques, libera um poder que ignora qualquer defesa.',
           'EQUIPMENT',
           'LEGENDARY',
           NULL,
           'speed',
           '{
                "SPEED": 10,
                "ATK": 15
           }',
           '[
                {
                    "type": "DAMAGE",
                    "value": 20,
                    "trigger": "ON_HIT",
                    "target": "ENEMY",
                    "params": {
                        "trueDamage": true,
                        "canCrit": false,
                        "canBeDodged": true,
                        "everyHits": 3
                    }
                }
           ]'
       );
