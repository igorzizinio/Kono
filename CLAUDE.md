# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

**Kono** is a Kotlin-based Discord bot with a built-in card-collecting RPG game system and a small Ktor web server. It
uses Kord for Discord, Ktorm for PostgreSQL, and Koin for dependency injection.

## Build & Run Commands

```bash
# Build
./gradlew build

# Run locally (requires config.properties or env vars)
./gradlew run

# Build fat JAR (output: build/libs/KonoBot.jar)
./gradlew shadowJar
# or
./gradlew stage

# Run tests
./gradlew test

# Start local Postgres (port 2504) via Docker
docker-compose up postgres
```

## Configuration

The bot reads from `config.properties` in the working directory, or falls back to environment variables:

- `TOKEN` — Discord bot token
- `RIOT_API_KEY` — Riot Games API key
- `DATABASE_URL` — PostgreSQL connection URL (Heroku-style `postgres://user:pass@host:port/db` or
  `jdbc:postgresql://...`)

## Architecture

### Entry point

`Main.kt` (`Launcher`) starts two coroutines in parallel: the Discord bot (`Kono.start()`) and the Ktor HTTP server (
`Server.start()` on port 8080). Koin is initialized here and `Kord` is passed in as a module.

### Dependency Injection

All singletons and factories are declared in `di/KoinModule.kt` (`appModule`). New commands, services, handlers, and
repositories must be registered here to be wired up. Text commands bind to `BaseCommand`, slash commands to
`KonoSlashCommand`.

### Command system

Two types:

- **Text commands** (`BaseCommand`) — triggered by `@Kono <command_name>`. Parsed in `CommandManager.handleCommand`. The
  `CommandCategory.Developer` category is hard-coded to bot owner only (Discord ID `477534823011844120`). Register by
  adding a `factoryOf(::MyCommand) { bind<BaseCommand>() }` in the Koin module.

- **Slash commands** (`KonoSlashCommand` / `KonoSlashSubCommand`) — Discord application commands. Register via
  `factoryOf(::MyCommand) { bind<KonoSlashCommand>() }`. Slash commands with sub-commands extend `KonoSlashCommand` and
  define `KonoSlashSubCommand` instances. Slash commands are pushed to Discord in `CommandManager.registerCommands()`
  which is called from `ReadyHandler`.

### Game domain: cards & combat

The card game is the main feature. The key layers:

- **`domain/card/CardCatalog.kt`** — static in-memory catalog of all `CardDefinition`s. Adding a new card means adding a
  new `CardDefinition` entry here. Cards have a `CardType` (CHARACTER or EQUIPMENT), `Rarity`, `baseStats`,
  `statsPerLevel`, and a list of `Ability`.

- **`domain/card/ability/`** — ability system. Each `Ability` has a `trigger` (`AbilityTrigger`: `OnBattleStart`,
  `OnTurnStart`, `OnAttackEvery(n)`, `OnBellowHealth(threshold)`, etc.) and a list of `Effect`s (sealed class: `Damage`,
  `Heal`, `BuffStat`, `ExecuteBellowHealth`, `Custom`, etc.).

- **`domain/battle/EnemyTeamCatalog.kt`** — static catalog of enemy teams players can fight against. Each
  `EnemyTeamDefinition` lists members by card ID + level and gives an `essenceReward`.

- **`engine/combat/CombatEngine.kt`** — the turn-based combat simulation. It operates on a `CombatState` (mutable, holds
  both teams, event queue, combat log, coin economy, temporary modifiers, etc.). The engine uses an event queue pattern:
  actions are enqueued as `CombatEvent`s, then `drainQueue()` processes each event by (1) checking all unit abilities
  for triggers, then (2) resolving the core event effect. Unit speed determines attack order each turn.

- **`domain/team/`** — domain handlers for team management operations (equip, unequip, upgrade, set active character).
  These are called from Discord commands.

### Data layer

- `data/DatabaseManager.kt` — creates the Ktorm `Database` connection from config.
- `data/entities/` — Ktorm table schema definitions.
- `data/repositories/` — database access objects.
- `data/dto/` — serializable data transfer objects for the HTTP API.
- `src/main/resources/db/create_db.sql` — full schema DDL. Run this to (re-)create the database from scratch (it drops
  and recreates all tables).

### Web server

`Server.kt` runs a Ktor/Netty server on port 8080. It serves static files from `resources/public` and HTML views from
`resources/views`, and exposes a `/random-messages/api` REST endpoint.

### Services

`services/` contains business logic used by commands:

- `GachaService` — card pull logic with pity counters
- `TeamBattleService` — orchestrates fights against `EnemyTeamCatalog` entries
- `CardService`, `UserService`, `DailyService`, `WorkService` — economy and progression
- `RiotService` — wraps R4J (Riot API library) for LoL commands
