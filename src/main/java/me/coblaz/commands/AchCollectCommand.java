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
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class AchCollectCommand extends AbstractPlayerCommand {

    // Positional required argument: /ach-collect <id>
    // ArgTypes.STRING accepts any text; no validator needed since
    // AchievementRegistry.forceCollect() handles unknown IDs gracefully.
    private final RequiredArg<String> idArg;

    public AchCollectCommand() {
        super("ach-collect", "Force-collect an achievement: /ach-collect <id>", false);
        this.idArg = withRequiredArg("id", "The achievement ID to force-collect", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext      ctx,
            @NonNullDecl Store<EntityStore>  store,
            @NonNullDecl Ref<EntityStore>    ref,
            @NonNullDecl PlayerRef           playerRef,
            @NonNullDecl World               world
    ) {
        String achievementId = idArg.get(ctx); // never null — it's a RequiredArg

        boolean ok = AchievementRegistry.getInstance().forceCollect(playerRef, achievementId);

        if (ok) {
            AchievementDefinition def =
                    AchievementRegistry.getInstance().findDefinition(achievementId);
            reply(playerRef, "Achievement collected!", def != null ? def.getTitle() : achievementId);
        } else {
            reply(playerRef, "Unknown or already collected:", achievementId);
        }
    }

    private void reply(@Nonnull PlayerRef playerRef,
                       @Nonnull String title,
                       @Nonnull String subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef, Message.raw(title), Message.raw(subtitle), true
        );
    }
}