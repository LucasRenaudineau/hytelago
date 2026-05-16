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
import me.coblaz.achievements.Registries;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class ArchSetCountCommand extends AbstractPlayerCommand {

    private final RequiredArg<Integer> countArg;
    private final RequiredArg<String>  idArg;

    public ArchSetCountCommand() {
        super("arch-set_count", "Set achievement count: /arch-set_count <count> <achievement_id>", false);
        this.countArg = withRequiredArg("count",          "The new count value",  ArgTypes.INTEGER);
        this.idArg    = withRequiredArg("achievement_id", "The achievement ID",   ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext      ctx,
            @NonNullDecl Store<EntityStore>  store,
            @NonNullDecl Ref<EntityStore>    ref,
            @NonNullDecl PlayerRef           playerRef,
            @NonNullDecl World               world
    ) {
        int    count = countArg.get(ctx);
        String id    = idArg.get(ctx);

        // Search both registries
        AchievementDefinition def = Registries.LOCATIONS.findDefinition(id);
        if (def != null) {
            Registries.LOCATIONS.setCount(playerRef, id, count);
        } else {
            def = Registries.ITEMS.findDefinition(id);
            if (def != null) {
                Registries.ITEMS.setCount(playerRef, id, count);
            }
        }

        if (def == null) {
            reply(playerRef, "Unknown achievement:", id);
            return;
        }

        reply(playerRef, "Count set to " + count, def.getTitle() + " [" + id + "]");
    }

    private void reply(@Nonnull PlayerRef playerRef,
                       @Nonnull String title,
                       @Nonnull String subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef, Message.raw(title), Message.raw(subtitle), true
        );
    }
}