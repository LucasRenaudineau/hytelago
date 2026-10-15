package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import me.coblaz.archipelago.ArchipelagoManager;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * The goal location: done once percent_goal % of the seed's other locations
 * are collected. Its needed count depends on the seed, see
 * {@link me.coblaz.archipelago.ArchipelagoManager#getGoalNeeded(String)}.
 * Collecting it sends the goal to the Archipelago server (releasing the player).
 *
 * With the kill_gobliterator goal, this location is not part of the seed and
 * the Gobliterator's phase 3 kill releases the player instead.
 */
public final class GoalAchievement {

    public static final String ID              = "complete_x_percent";
    public static final String GOBLITERATOR_ID = "kill_goblin_gobliterator_phase3";

    public static void register(@Nonnull AchievementRegistry reg) {
        reg.registerAchievement(new AchievementDefinition(ID, "Collected Achievements", 1, Category.GOAL));
    }

    /** True when {@code achievementId} is the location that releases this player. */
    public static boolean isGoal(@Nonnull PlayerRef playerRef, @Nonnull String achievementId) {
        String goalId = ArchipelagoManager.INSTANCE.isGobliteratorGoal(playerRef) ? GOBLITERATOR_ID : ID;
        return goalId.equals(achievementId);
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
