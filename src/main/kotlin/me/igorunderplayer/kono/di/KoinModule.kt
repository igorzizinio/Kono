package me.igorunderplayer.kono.di



import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.text.testing.*
import me.igorunderplayer.kono.commands.text.dev.*
import me.igorunderplayer.kono.commands.text.lol.*
import me.igorunderplayer.kono.commands.text.`fun`.*
import me.igorunderplayer.kono.commands.slash.testing.Info as SlashInfo
import me.igorunderplayer.kono.commands.slash.testing.Avatar as SlashAvatar
import me.igorunderplayer.kono.commands.slash.testing.DestinoCommand as SlashDestino
import me.igorunderplayer.kono.commands.slash.testing.Register as SlashRegister
import me.igorunderplayer.kono.commands.slash.lol.LoLCommands as SlashLoLCommands
import me.igorunderplayer.kono.commands.slash.image.ImageCommands as SlashImageCommands
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.repositories.*
import me.igorunderplayer.kono.services.*

import org.koin.core.module.dsl.*
import org.koin.dsl.module

val appModule = module {

    // ========================
    // DATABASE
    // ========================
    singleOf(::DatabaseManager)

    // ========================
    // REPOSITORIES
    // ========================
    singleOf(::UserRepository)
    singleOf(::RandomMessageRepository)

    // ========================
    // SERVICES
    // ========================
    singleOf(::UserService)
    singleOf(::RandomMessageService)
    singleOf(::RiotService)

    // ========================
    // TEXT COMMANDS
    // ========================

    // testing
    factoryOf(::Avatar) { bind<BaseCommand>() }
    factoryOf(::Info) { bind<BaseCommand>() }
    factoryOf(::Help) { bind<BaseCommand>() }

    factoryOf(::Profile) { bind<BaseCommand>() }
    factoryOf(::DestinoTextCommand) { bind<BaseCommand>() }
    factoryOf(::BorderGradient) { bind<BaseCommand>() }
    factoryOf(::TinderCommand) { bind<BaseCommand>() }

    factoryOf(::Clear) { bind<BaseCommand>() }

    // lol
    factoryOf(::LoLChampion) { bind<BaseCommand>() }
    factoryOf(::LoLMatches) { bind<BaseCommand>() }

    // dev
    factoryOf(::GuildsCommand) { bind<BaseCommand>() }
    factoryOf(::DeleteApplicationCommand) { bind<BaseCommand>() }

    // fun
    factoryOf(::HCommand) { bind<BaseCommand>() }

    // ========================
    // SLASH COMMANDS
    // ========================

    factoryOf(::SlashInfo) { bind<KonoSlashCommand>() }
    factoryOf(::SlashAvatar) { bind<KonoSlashCommand>() }
    factoryOf(::SlashRegister) { bind<KonoSlashCommand>() }
    factoryOf(::SlashDestino) { bind<KonoSlashCommand>() }

    factoryOf(::SlashLoLCommands) { bind<KonoSlashCommand>() }

    factoryOf(::SlashImageCommands) { bind<KonoSlashCommand>() }


    // ========================
    // MANAGER
    // ========================
    single {
        CommandManager(
            kord = get(),
            commands = getAll<BaseCommand>(),
            slashCommands = getAll<KonoSlashCommand>()
        )
    }
}
