# Copilot instructions for Kono

## Build, run, and test

This is a Kotlin/JVM 21 Gradle project. Use the Gradle wrapper from the repository root:

```bash
./gradlew build
./gradlew test
./gradlew run
./gradlew shadowJar       # produces build/libs/KonoBot.jar
./gradlew stage           # alias that depends on shadowJar
```

Run one test class or method with Gradle's `--tests` filter:

```bash
./gradlew test --tests 'fully.qualified.TestClassName'
./gradlew test --tests 'fully.qualified.TestClassName.testMethod'
```

Tests use JUnit Platform through `kotlin("test")`. There is currently no dedicated lint or formatting task configured in `build.gradle.kts`; keep Kotlin formatting consistent with nearby code.

Local runtime configuration is required. `Config.load()` reads `config.properties` first, otherwise it requires these environment variables: `TOKEN`, `RIOT_API_KEY`, `DATABASE_URL`, `OPEN_ROUTER_API_KEY`, and `OPEN_ROUTER_MODEL`. `DATABASE_URL` may be a JDBC PostgreSQL URL or a Heroku-style `postgres://...` URL. For local PostgreSQL, use `docker-compose up postgres` (port 2504 as configured by the compose file).

## Architecture

- `Launcher` (`src/main/kotlin/me/igorunderplayer/kono/Main.kt`) loads configuration, creates the Kord client, starts Koin, and runs the Discord bot and Ktor server concurrently.
- `Kono` starts Kord event listeners and the `CommandManager`; `Server` owns the Netty HTTP server on port 8080 and serves `src/main/resources/public` and `src/main/resources/views`.
- `di/KoinModule.kt` is the runtime composition root. Repositories, services, handlers, commands, and slash commands must be registered there for constructor injection to work. `Main.kt` adds the Kord and AI client bindings.
- Discord input has two paths: text commands extend `BaseCommand` and are parsed by `CommandManager.handleCommand`; application commands extend `KonoSlashCommand` (with optional `KonoSlashSubCommand`s) and are dispatched from interaction events. Slash command definitions are pushed globally after the Discord `Ready` event.
- The game domain is split between static definitions and stateful orchestration. `domain/card/CardCatalog.kt` is the source of card definitions and abilities; `domain/battle/EnemyTeamCatalog.kt` defines bot teams; `domain/team/` builds and mutates team state; `services/` coordinates persistence and gameplay; `engine/combat/CombatEngine.kt` executes turn-based battles.
- Combat is event-driven: `CombatState` holds mutable teams, modifiers, logs, and queued events; `CombatEngine` orders living units by speed, enqueues turn/attack events, drains the queue, and applies ability triggers/effects.
- Persistence uses Ktorm over PostgreSQL. `data/entities/` describes tables, `data/repositories/` performs database access, and services provide the application-facing operations. `src/main/resources/db/create_db.sql` is a destructive full schema reset and must not be run against data that should be preserved.

## Repository conventions

- When adding a runtime component, prefer constructor injection and add its `singleOf`, `factoryOf`, or explicit binding in `di/KoinModule.kt`.
- Register new text commands as `factoryOf(::Command) { bind<BaseCommand>() }`; register new slash commands as `factoryOf(::Command) { bind<KonoSlashCommand>() }`. Slash subcommands belong to their parent slash command.
- Text command names and aliases are matched case-insensitively. `Developer` text commands are restricted by the existing owner-ID check, and `Management` commands require Manage Guild or Administrator permissions; preserve these checks when changing command categories.
- Keep card and enemy definitions in their catalogs rather than duplicating definitions in commands or services. Card IDs are normalized by `CardCatalog.getById`; use the catalog's `CardType`, rarity, stats, and ability model.
- Put gameplay rules in domain handlers/services/engine layers, not in Discord event handlers. Commands should translate Discord input into service calls and present the result.
- Repository database work is suspendable and should follow the existing coroutine/IO pattern. Keep database schema changes synchronized with `src/main/resources/db/create_db.sql`.
- User-facing bot text is predominantly Portuguese; preserve the surrounding language and existing response style when editing command output.
- Do not commit `config.properties`, `.env`, credentials, or generated `build/` artifacts.
