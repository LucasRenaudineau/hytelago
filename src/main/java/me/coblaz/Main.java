package me.coblaz;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.packets.interface_.EventTitleStyle;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.*;
import me.coblaz.archipelago.ArchipelagoManager;
import me.coblaz.commands.*;
import me.coblaz.listeners.ArchipelagoTicker;
import me.coblaz.listeners.DeathListener;
import me.coblaz.listeners.KillListener;

import javax.annotation.Nonnull;

import me.coblaz.listeners.InventoryListener;

import me.coblaz.items.ItemsAchievements;

public class Main extends JavaPlugin {

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        AchievementRegistry locations = Registries.LOCATIONS;
        AchievementRegistry items     = Registries.ITEMS;

        // Goal (first, so it shows at the top of the table)
        GoalAchievement.register(locations);

        // General kill milestones
        locations.registerAchievement(new AchievementDefinition("first_kill",    "First Blood",       1, Category.GENERAL_KILLS));
        locations.registerAchievement(new AchievementDefinition("ten_kills",     "Fighter",          10, Category.GENERAL_KILLS));
        locations.registerAchievement(new AchievementDefinition("thirty_kills",  "Violent",          30, Category.GENERAL_KILLS));
        locations.registerAchievement(new AchievementDefinition("fifty_kills",   "Serial Killer",    50, Category.GENERAL_KILLS));
        locations.registerAchievement(new AchievementDefinition("seventy_kills", "Mass Murderer",    70, Category.GENERAL_KILLS));
        locations.registerAchievement(new AchievementDefinition("hundred_kills", "Mob Exterminator", 100, Category.GENERAL_KILLS));

        // Mob-specific kill achievements
        MobKillAchievements.registerAll(locations);
        LocationsPropertyAchievements.registerAll(locations);
        DeathAchievements.registerAll(locations);
        ItemsAchievements.registerAll(Registries.ITEMS);
        MemoriesAchievements.registerAll(locations);
        RegionsAchievements.registerAll(locations);

        // Listeners
        locations.addListener((playerRef, def) -> {
            System.out.printf(
                    "[ArchipelagoMod] Location achievement collected: '%s' (%s) by player %s%n",
                    def.getId(), def.getTitle(), playerRef.getUuid());

            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Achievement collected!"),
                    Message.raw(def.getTitle()),
                    EventTitleStyle.Major
            );

            // Send the location check to the Archipelago server
            ArchipelagoManager.INSTANCE.sendLocationCheck(playerRef, def.getId());

            // Collecting the goal location completes the game (releases the player)
            if (GoalAchievement.ID.equals(def.getId())) {
                ArchipelagoManager.INSTANCE.sendGoal(playerRef);
            }
        });
        items.addListener((playerRef, def) ->
                        EventTitleUtil.showEventTitleToPlayer(
                                playerRef,
                                Message.raw("Item collected!"),
                                Message.raw(def.getId()),
                                EventTitleStyle.Major
                        )
        );

        // Commands
        this.getCommandRegistry().registerCommand(
                new HelloTest("Hello", "Test command to say hello", false)
        );
        this.getCommandRegistry().registerCommand(new ArchLocationsCommand());
        this.getCommandRegistry().registerCommand(new ArchCollectCommand());
        this.getCommandRegistry().registerCommand(new ArchItemsCommand());
        this.getCommandRegistry().registerCommand(new ArchSetCountCommand());
        this.getCommandRegistry().registerCommand(new ArchSetStateCommand());
        this.getCommandRegistry().registerCommand(new ArchCollectFirstCommand());
        this.getCommandRegistry().registerCommand(new ArchSpawnCommand());
        this.getCommandRegistry().registerCommand(new ArchHelpCommand());

        // Archipelago connection command
        this.getCommandRegistry().registerCommand(new ArchConnectCommand());
        this.getCommandRegistry().registerCommand(new ArchDeathLinkCommand());

        // Systems
        EntityStore.REGISTRY.registerSystem(new KillListener());
        EntityStore.REGISTRY.registerSystem(new InventoryListener());
        EntityStore.REGISTRY.registerSystem(new DeathListener());
        EntityStore.REGISTRY.registerSystem(new ArchipelagoTicker());
    }
}