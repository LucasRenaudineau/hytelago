package me.coblaz.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.archipelago.ArchipelagoManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

/**
 * /arch-death_link &lt;on|off&gt;
 *
 * Toggles DeathLink for the executing player. When on, dying in Hytale sends a
 * death to the Archipelago server, and incoming deaths kill the player.
 */
public class ArchDeathLinkCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> stateArg;

    public ArchDeathLinkCommand() {
        super("arch-death_link",
                "Toggle DeathLink: /arch-death_link <on|off>",
                false);
        this.stateArg = withRequiredArg("state", "on or off", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext     ctx,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore>   ref,
            @NonNullDecl PlayerRef          playerRef,
            @NonNullDecl World              world
    ) {
        String stateStr = stateArg.get(ctx).toLowerCase();

        boolean enabled;
        if (stateStr.equals("on")) {
            enabled = true;
        } else if (stateStr.equals("off")) {
            enabled = false;
        } else {
            showTitle(playerRef, "DeathLink", "Use 'on' or 'off'");
            return;
        }

        boolean applied = ArchipelagoManager.INSTANCE.setDeathLink(playerRef, enabled);
        if (applied) {
            showTitle(playerRef, "DeathLink", enabled ? "Enabled" : "Disabled");
        } else {
            showTitle(playerRef, "DeathLink", "Not connected to Archipelago");
        }
    }

    private void showTitle(@Nonnull PlayerRef playerRef,
                           @Nonnull String    title,
                           @Nonnull String    subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(title),
                Message.raw(subtitle),
                true
        );
    }
}
