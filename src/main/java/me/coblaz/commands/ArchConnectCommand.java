package me.coblaz.commands;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
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
 * /arch-connect &lt;ip&gt; &lt;port&gt; &lt;slot_name&gt; [--password &lt;password&gt;]
 *
 * Connects the executing player to an Archipelago multiworld server.
 * If a previous connection exists for that player it is closed first.
 * Progress is resumed automatically from the last saved item index.
 */
public class ArchConnectCommand extends AbstractPlayerCommand {
    private final RequiredArg<String>  ipArg;
    private final RequiredArg<Integer> portArg;
    private final RequiredArg<String>  slotArg;
    // UNVERIFIED: DefaultArg / withDefaultArg is based on community docs, not
    // official ones. See chat notes for how to confirm this in IntelliJ before relying on it.
    private final DefaultArg<String>   passwordArg;

    public ArchConnectCommand() {
        super("arch-connect",
                "Connect to Archipelago: /arch-connect <ip> <port> <slot_name> [--password <password>]",
                false);
        this.ipArg   = withRequiredArg("ip",        "Server IP address",   ArgTypes.STRING);
        this.portArg = withRequiredArg("port",       "Server port (38281)", ArgTypes.INTEGER);
        this.slotArg = withRequiredArg("slot_name",  "Your AP slot name",   ArgTypes.STRING);
        // Matched as: --password <value>. get() should never return null here -
        // it falls back to "" when the flag isn't used at all.
        this.passwordArg = withDefaultArg(
                "password",
                "Room password, if the server requires one",
                ArgTypes.STRING,
                "",   // default value when --password is omitted
                ""    // default's display text for /help
        );
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
        String password = passwordArg.get(ctx);

        // Removes " or ' from beginning and end of slotName string to allow them to be used for slot names with spaces
        int nameLength = slotName.length();
        if ((slotName.charAt(0) == '\"' && slotName.charAt(nameLength - 1) == '\"') || (slotName.charAt(0) == '\'' && slotName.charAt(nameLength - 1) == '\'')) {
            slotName = slotName.substring(1, nameLength - 1);
        }

        // Show "connecting" immediately. The actual outcome is asynchronous:
        // connect() only opens the WebSocket, and the server may still reject the
        // slot (or be unreachable). Success/failure is reported later, once the
        // server answers — see ArchipelagoManager.HytaleAPClient.onConnectionResult.
        showTitle(playerRef, "Archipelago", "Connecting to " + ip + ":" + port + " ...");
        try {
            ArchipelagoManager.INSTANCE.connect(playerRef, ref, store, ip, port, slotName, password);
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
