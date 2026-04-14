package me.igorunderplayer.kono.di

import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.text.InventoryCommand
import me.igorunderplayer.kono.commands.text.PullCommand
import me.igorunderplayer.kono.commands.text.dev.DeleteApplicationCommand
import me.igorunderplayer.kono.commands.text.dev.GuildsCommand
import me.igorunderplayer.kono.commands.text.`fun`.HCommand
import me.igorunderplayer.kono.commands.text.`fun`.TinderCommand
import me.igorunderplayer.kono.commands.text.lol.LoLChampion
import me.igorunderplayer.kono.commands.text.lol.LoLMatches
import me.igorunderplayer.kono.commands.text.testing.*
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.data.repositories.RandomMessageRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.events.ChatInputCommandInteractionCreateHandler
import me.igorunderplayer.kono.events.EventManager
import me.igorunderplayer.kono.events.MessageCreateHandler
import me.igorunderplayer.kono.events.handlers.ReadyHandler
import me.igorunderplayer.kono.services.CardService
import me.igorunderplayer.kono.services.DailyService
import me.igorunderplayer.kono.services.EmojiService
import me.igorunderplayer.kono.services.GachaService
import me.igorunderplayer.kono.services.RandomMessageService
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import me.igorunderplayer.kono.services.WorkService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import me.igorunderplayer.kono.commands.slash.image.ImageCommands as SlashImageCommands
import me.igorunderplayer.kono.commands.slash.lol.LoLCommands as SlashLoLCommands
import me.igorunderplayer.kono.commands.slash.testing.Avatar as SlashAvatar
import me.igorunderplayer.kono.commands.slash.testing.DestinoCommand as SlashDestino
import me.igorunderplayer.kono.commands.slash.testing.Info as SlashInfo
import me.igorunderplayer.kono.commands.slash.testing.Register as SlashRegister

val appModule = module {

    // ========================
    // KONO
    // ========================
    singleOf(::Kono)


    // ========================
    // EVENTS
    // ========================
    singleOf(::EventManager)

    singleOf(::ReadyHandler)
    singleOf(::MessageCreateHandler)
    singleOf(::ChatInputCommandInteractionCreateHandler)


    // ========================
    // DATABASE
    // ========================
    singleOf(::DatabaseManager)

    // ========================
    // REPOSITORIES
    // ========================
    singleOf(::UserRepository)
    singleOf(::RandomMessageRepository)
    singleOf(::CardRepository)
    singleOf(::CardInstanceRepository)

    // ========================
    // SERVICES
    // ========================
    singleOf(::UserService)
    singleOf(::RandomMessageService)
    singleOf(::RiotService)
    singleOf(::EmojiService)
    singleOf(::DailyService)
    singleOf(::WorkService)
    singleOf(::CardService)
    singleOf(::GachaService)

    // ========================
    // TEXT COMMANDS
    // ========================

    // testing
    factoryOf(::Avatar) { bind<BaseCommand>() }
    factoryOf(::Info) { bind<BaseCommand>() }
    factoryOf(::Help) { bind<BaseCommand>() }
    factoryOf(::RegisterCommand) { bind<BaseCommand>() }

    factoryOf(::Profile) { bind<BaseCommand>() }
    factoryOf(::DestinoTextCommand) { bind<BaseCommand>() }
    factoryOf(::BorderGradient) { bind<BaseCommand>() }
    factoryOf(::TinderCommand) { bind<BaseCommand>() }
    factoryOf(::KissCommand) { bind<BaseCommand>() }

    factoryOf(::Clear) { bind<BaseCommand>() }

    // lol
    factoryOf(::LoLChampion) { bind<BaseCommand>() }
    factoryOf(::LoLMatches) { bind<BaseCommand>() }

    // dev
    factoryOf(::GuildsCommand) { bind<BaseCommand>() }
    factoryOf(::DeleteApplicationCommand) { bind<BaseCommand>() }

    // fun
    factoryOf(::HCommand) { bind<BaseCommand>() }

    // testing eKONOmy
    factoryOf(::DailyCommand) { bind<BaseCommand>() }
    factoryOf(::WorkCommand) { bind<BaseCommand>() }
    factoryOf(::PullCommand) { bind<BaseCommand>() }
    factoryOf(::InventoryCommand) { bind<BaseCommand>() }

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
