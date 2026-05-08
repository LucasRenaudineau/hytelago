package me.coblaz;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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

        // Your command – untouched
        this.getCommandRegistry().registerCommand(
                new HelloTest("Hello", "Test command to say hello", false)
        );

        // Register the kill listener system globally.
        // Every future (and already created) world will now run this system.
        EntityStore.REGISTRY.registerSystem(new KillListener());
    }
}