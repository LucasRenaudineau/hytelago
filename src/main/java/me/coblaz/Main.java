package me.coblaz;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.*;
import me.coblaz.commands.AchCollectCommand;
import me.coblaz.commands.AchListCommand;
import me.coblaz.commands.HelloTest;
import me.coblaz.listeners.DeathListener;
import me.coblaz.listeners.KillListener;

import javax.annotation.Nonnull;

import me.coblaz.listeners.InventoryListener;

public class Main extends JavaPlugin {

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        AchievementRegistry reg = AchievementRegistry.getInstance();

        // ── General kill milestones ───────────────────────────────────────────
        reg.registerAchievement(new AchievementDefinition("first_kill",    "First Blood",      1));
        reg.registerAchievement(new AchievementDefinition("ten_kills",     "Serial Killer",    10));
        reg.registerAchievement(new AchievementDefinition("hundred_kills", "Mass Destruction", 100));
        reg.registerAchievement(new AchievementDefinition("finding_frost_dragon", "Found Dragon", 1));

        // ── Mob-specific kill achievements ────────────────────────────────────
        MobKillAchievements.registerAll(reg);
        // ── Smelting achievements ─────────────────────────────────────────────────
        // SmeltingAchievements.registerAll(reg);   // ← ADD
        ItemAchievements.registerAll(reg);
        DeathAchievements.registerAll(reg);

        // ── Listeners ─────────────────────────────────────────────────────────────
        reg.addListener((playerRef, def) ->
                EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        Message.raw("Achievement collected!"),
                        Message.raw(def.getId()),
                        true
                )
                // In theory here should be some code to send the check of location with the id to the archipelago server
        );

        // ── Commands ──────────────────────────────────────────────────────────
        this.getCommandRegistry().registerCommand(
                new HelloTest("Hello", "Test command to say hello", false)
        );
        this.getCommandRegistry().registerCommand(new AchListCommand());
        this.getCommandRegistry().registerCommand(new AchCollectCommand());

        // ── Systems ───────────────────────────────────────────────────────────
        EntityStore.REGISTRY.registerSystem(new KillListener());
        EntityStore.REGISTRY.registerSystem(new InventoryListener());   // ← ADD
        EntityStore.REGISTRY.registerSystem(new DeathListener());
    }
}