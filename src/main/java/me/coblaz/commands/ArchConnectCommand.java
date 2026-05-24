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
 * /arch-connect &lt;ip&gt; &lt;port&gt; &lt;slot_name&gt;
 *
 * Connects the executing player to an Archipelago multiworld server.
 * If a previous connection exists for that player it is closed first.
 * Progress is resumed automatically from the last saved item index.
 */
public class ArchConnectCommand extends AbstractPlayerCommand {

    private final RequiredArg<String>  ipArg;
    private final RequiredArg<Integer> portArg;
    private final RequiredArg<String>  slotArg;

    public ArchConnectCommand() {
        super("arch-connect",
                "Connect to Archipelago: /arch-connect <ip> <port> <slot_name>",
                false);
        this.ipArg   = withRequiredArg("ip",        "Server IP address",   ArgTypes.STRING);
        this.portArg = withRequiredArg("port",       "Server port (38281)", ArgTypes.INTEGER);
        this.slotArg = withRequiredArg("slot_name",  "Your AP slot name",   ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext     ctx,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore>   ref,
            @NonNullDecl PlayerRef          playerRef,
            @NonNullDecl World              world
    ) {
        String ip       = ipArg.get(ctx);
        int    port     = portArg.get(ctx);
        String slotName = slotArg.get(ctx);

        // Optimistic feedback: show "connecting" immediately
        showTitle(playerRef, "Archipelago", "Connecting to " + ip + ":" + port + " …");

        try {
            ArchipelagoManager.INSTANCE.connect(playerRef, ref, store, ip, port, slotName);
            showTitle(playerRef, "Archipelago", "Connected as " + slotName);
        } catch (RuntimeException ex) {
            // ArchipelagoManager wraps URISyntaxException in a RuntimeException
            showTitle(playerRef, "Connection failed", ex.getMessage());
        }
    }

    // Helper

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