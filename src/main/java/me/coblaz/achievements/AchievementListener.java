package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface AchievementListener {
    void onAchievementCollected(
            @Nonnull PlayerRef playerRef,
            @Nonnull AchievementDefinition achievement
    );
}