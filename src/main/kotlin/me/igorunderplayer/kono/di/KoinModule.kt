package me.igorunderplayer.kono.di

import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.text.testing.InventoryCommand
import me.igorunderplayer.kono.commands.text.testing.PullCommand
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
import me.igorunderplayer.kono.commands.slash.image.ImageSlashCommands
import me.igorunderplayer.kono.commands.slash.lol.LoLSlashCommands
import me.igorunderplayer.kono.commands.slash.testing.AvatarSlashCommand
import me.igorunderplayer.kono.commands.slash.testing.DestinoSlashCommand
import me.igorunderplayer.kono.commands.slash.testing.InfoSlashCommand
import me.igorunderplayer.kono.commands.slash.testing.RegisterSlashCommand
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.EquipItemHandler
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler
import me.igorunderplayer.kono.domain.team.UnequipItemHandler

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
    // Card Game Engine
    // ========================
    singleOf(::BuildUnitHandler)
    singleOf(::EquipItemHandler)
    singleOf(::SetActiveCharacterHandler)
    singleOf(::UnequipItemHandler)

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
    factoryOf(::EquipItemCommand) { bind<BaseCommand>() }
    factoryOf(::EquipListCommand) { bind<BaseCommand>() }
    factoryOf(::UnequipCommand) { bind<BaseCommand>() }
    factoryOf(::TopCommand) { bind<BaseCommand>() }
    factoryOf(::FightCommand) { bind<BaseCommand>() }
    factoryOf(::SetActiveCommand) { bind<BaseCommand>() }

    // ========================
    // SLASH COMMANDS
    // ========================

    factoryOf(::InfoSlashCommand) { bind<KonoSlashCommand>() }
    factoryOf(::AvatarSlashCommand) { bind<KonoSlashCommand>() }
    factoryOf(::RegisterSlashCommand) { bind<KonoSlashCommand>() }
    factoryOf(::DestinoSlashCommand) { bind<KonoSlashCommand>() }
    factoryOf(::LoLSlashCommands) { bind<KonoSlashCommand>() }
    factoryOf(::ImageSlashCommands) { bind<KonoSlashCommand>() }


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
