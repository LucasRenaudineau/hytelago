package me.coblaz;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.commands.AchCollectCommand;
import me.coblaz.commands.AchListCommand;
import me.coblaz.commands.HelloTest;
import me.coblaz.listeners.KillListener;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        // ── Register achievements (example set) ───────────────────────────────
        AchievementRegistry reg = AchievementRegistry.getInstance();

        reg.registerAchievement(new AchievementDefinition(
                "first_kill",   "First Blood",      1
        ));
        reg.registerAchievement(new AchievementDefinition(
                "ten_kills",    "Serial Killer",    10
        ));
        reg.registerAchievement(new AchievementDefinition(
                "hundred_kills","Mass Destruction",100
        ));
        reg.registerAchievement(new AchievementDefinition(
                "kill_zombie", "Kill Zombie", 2
        ));

        // ── Commands ─────────────────────────────────────────────────────────
        this.getCommandRegistry().registerCommand(
                new HelloTest("Hello", "Test command to say hello", false)
        );
        this.getCommandRegistry().registerCommand(new AchListCommand());
        this.getCommandRegistry().registerCommand(new AchCollectCommand());

        // ── Systems ──────────────────────────────────────────────────────────
        EntityStore.REGISTRY.registerSystem(new KillListener());
    }
}