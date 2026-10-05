package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * The goal location: done once percent_goal % of the seed's other locations
 * are collected. Its needed count depends on the seed, see
 * {@link me.coblaz.archipelago.ArchipelagoManager#getGoalNeeded(String)}.
 * Collecting it sends the goal to the Archipelago server (releasing the player).
 */
public final class GoalAchievement {

    public static final String ID = "complete_x_percent";

    public static void register(@Nonnull AchievementRegistry reg) {
        reg.registerAchievement(new AchievementDefinition(ID, "Collected Achievements", 1));
    }

    /** Sets the goal's count to the number of other seed locations that are COLLECTED. */
    public static void syncCount(@Nonnull AchievementRegistry reg,
                                 @Nonnull PlayerRef playerRef,
                                 @Nonnull Set<String> visibleIds) {
        int collected = 0;
        for (AchievementDefinition def : reg.getDefinitions()) {
            if (ID.equals(def.getId()) || !visibleIds.contains(def.getId())) continue;
            if (reg.getData(playerRef, def.getId()).getStatus() == AchievementStatus.COLLECTED) {
                collected++;
            }
        }
        reg.setCount(playerRef, ID, collected);
    }

    private GoalAchievement() {}
}
