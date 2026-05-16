package me.coblaz.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.AchievementStatus;
import me.coblaz.achievements.Registries;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class ArchSetStateCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> idArg;
    private final RequiredArg<String> stateArg;

    public ArchSetStateCommand() {
        super("arch-set_state",
                "Set achievement state: /arch-set_state <achievement_id> <NOT_DONE|DONE|COLLECTED>",
                false);
        this.idArg    = withRequiredArg("achievement_id", "The achievement ID",                    ArgTypes.STRING);
        this.stateArg = withRequiredArg("state",          "NOT_DONE, DONE, or COLLECTED",          ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext      ctx,
            @NonNullDecl Store<EntityStore>  store,
            @NonNullDecl Ref<EntityStore>    ref,
            @NonNullDecl PlayerRef           playerRef,
            @NonNullDecl World               world
    ) {
        String id        = idArg.get(ctx);
        String stateStr  = stateArg.get(ctx).toUpperCase();

        // Parse state
        AchievementStatus status;
        try {
            status = AchievementStatus.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            reply(playerRef,
                    "Invalid state: " + stateStr,
                    "Use NOT_DONE, DONE, or COLLECTED");
            return;
        }

        // Find which registry owns this achievement
        AchievementDefinition def      = Registries.LOCATIONS.findDefinition(id);
        AchievementRegistry   registry = Registries.LOCATIONS;
        if (def == null) {
            def      = Registries.ITEMS.findDefinition(id);
            registry = Registries.ITEMS;
        }

        if (def == null) {
            reply(playerRef, "Unknown achievement:", id);
            return;
        }

        registry.setStatus(playerRef, id, status);
        reply(playerRef,
                "State set to " + status.name(),
                def.getTitle() + " [" + id + "]");
    }

    private void reply(@Nonnull PlayerRef playerRef,
                       @Nonnull String title,
                       @Nonnull String subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef, Message.raw(title), Message.raw(subtitle), true
        );
    }
}