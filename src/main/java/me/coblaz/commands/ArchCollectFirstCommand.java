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
import com.hypixel.hytale.protocol.packets.interface_.EventTitleStyle;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementStatus;
import me.coblaz.achievements.GoalAchievement;
import me.coblaz.achievements.Registries;
import me.coblaz.archipelago.ArchipelagoManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

/**
 * Testing command: sets the first n locations of the table (the goal excluded)
 * to DONE. Already DONE or COLLECTED rows are left as they are.
 */
public class ArchCollectFirstCommand extends AbstractPlayerCommand {

    private final RequiredArg<Integer> nArg;

    public ArchCollectFirstCommand() {
        super("arch-collect_first",
                "Set the first n locations to DONE: /arch-collect_first <n>",
                false);
        this.nArg = withRequiredArg("n", "Number of locations to set to DONE", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext      ctx,
            @NonNullDecl Store<EntityStore>  store,
            @NonNullDecl Ref<EntityStore>    ref,
            @NonNullDecl PlayerRef           playerRef,
            @NonNullDecl World               world
    ) {
        // The table only exists once connected to a seed.
        if (!ArchipelagoManager.INSTANCE.isConnected(playerRef)) {
            reply(playerRef, "Not connected to Archipelago.", "Run /arch-connect first.");
            return;
        }

        int n    = nArg.get(ctx);
        int seen = 0;
        for (AchievementDefinition def : Registries.LOCATIONS.getDefinitions()) {
            if (seen >= n) break;
            String id = def.getId();
            if (GoalAchievement.isGoal(playerRef, id)
                    || !ArchipelagoManager.INSTANCE.isLocationActive(playerRef, id)) continue;

            if (Registries.LOCATIONS.getData(playerRef, id).getStatus() == AchievementStatus.NOT_DONE) {
                Registries.LOCATIONS.setStatus(playerRef, id, AchievementStatus.DONE);
            }
            seen++;
        }

        reply(playerRef, "First " + seen + " locations set to DONE", "Open /arch-locations to collect them");
    }

    private void reply(@Nonnull PlayerRef playerRef,
                       @Nonnull String title,
                       @Nonnull String subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef, Message.raw(title), Message.raw(subtitle), EventTitleStyle.Major
        );
    }
}
