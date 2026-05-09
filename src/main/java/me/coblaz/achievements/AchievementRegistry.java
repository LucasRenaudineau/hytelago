package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class AchievementRegistry {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static final AchievementRegistry INSTANCE = new AchievementRegistry();
    private AchievementRegistry() {}

    @Nonnull
    public static AchievementRegistry getInstance() { return INSTANCE; }

    // ── Internal state ───────────────────────────────────────────────────────
    private final List<AchievementDefinition>                    definitions = new ArrayList<>();
    private final Map<String, Map<String, PlayerAchievementData>> playerData  = new HashMap<>();
    private final List<AchievementListener>                       listeners   = new ArrayList<>();

    // ── Registration (called at plugin startup) ──────────────────────────────

    public void registerAchievement(@Nonnull AchievementDefinition def) {
        boolean duplicate = definitions.stream().anyMatch(d -> d.getId().equals(def.getId()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "[AchievementMod] Duplicate achievement id: '" + def.getId() + "'"
            );
        }
        definitions.add(def);
    }

    /** Other mods call this to be notified when a player collects an achievement. */
    public void addListener(@Nonnull AchievementListener listener) {
        listeners.add(listener);
    }

    // ── Data access ──────────────────────────────────────────────────────────

    @Nonnull
    public List<AchievementDefinition> getDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    /** Returns (or creates) the mutable state for one player + one achievement. */
    @Nonnull
    public PlayerAchievementData getData(@Nonnull PlayerRef playerRef,
                                         @Nonnull String achievementId) {
        return getOrCreate(playerRef, achievementId);
    }

    @Nullable
    public AchievementDefinition findDefinition(@Nonnull String id) {
        return definitions.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst().orElse(null);
    }

    // ── Game logic ───────────────────────────────────────────────────────────

    /**
     * Increases the progress counter for a player's achievement.
     * Does NOT automatically flip it to DONE; call refreshStatuses() for that
     * (the UI does this automatically on open).
     */
    public void incrementCount(@Nonnull PlayerRef playerRef,
                               @Nonnull String achievementId,
                               int amount) {
        getOrCreate(playerRef, achievementId).incrementCount(amount);
    }

    /**
     * Directly overrides the status of a player's achievement.
     * Useful for admin commands or special game logic.
     */
    public void setStatus(@Nonnull PlayerRef playerRef,
                          @Nonnull String achievementId,
                          @Nonnull AchievementStatus status) {
        getOrCreate(playerRef, achievementId).setStatus(status);
    }

    /**
     * Scans all NOT_DONE achievements and flips them to DONE
     * if count >= neededCount. Called automatically when the UI opens.
     */
    public void refreshStatuses(@Nonnull PlayerRef playerRef) {
        for (AchievementDefinition def : definitions) {
            PlayerAchievementData data = getOrCreate(playerRef, def.getId());
            if (data.getStatus() == AchievementStatus.NOT_DONE
                    && data.getCount() >= def.getNeededCount()) {
                data.setStatus(AchievementStatus.DONE);
            }
        }
    }

    /**
     * Marks every DONE achievement as COLLECTED and fires all listeners.
     * Returns the list of achievements that were just collected (so the UI
     * can display them to the player).
     */
    @Nonnull
    public List<AchievementDefinition> collectDoneAchievements(@Nonnull PlayerRef playerRef) {
        List<AchievementDefinition> justCollected = new ArrayList<>();

        for (AchievementDefinition def : definitions) {
            PlayerAchievementData data = getOrCreate(playerRef, def.getId());
            if (data.getStatus() == AchievementStatus.DONE) {
                data.setStatus(AchievementStatus.COLLECTED);
                justCollected.add(def);
                fireListeners(playerRef, def);
            }
        }
        return justCollected;
    }

    /**
     * /ach-collect command: sets count = neededCount and immediately collects.
     * Returns false if the id is unknown or already collected.
     */
    public boolean forceCollect(@Nonnull PlayerRef playerRef,
                                @Nonnull String achievementId) {
        AchievementDefinition def = findDefinition(achievementId);
        if (def == null) return false;

        PlayerAchievementData data = getOrCreate(playerRef, achievementId);
        if (data.getStatus() == AchievementStatus.COLLECTED) return false;

        data.setCount(def.getNeededCount());
        data.setStatus(AchievementStatus.COLLECTED);
        fireListeners(playerRef, def);
        return true;
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private void fireListeners(@Nonnull PlayerRef playerRef,
                               @Nonnull AchievementDefinition def) {
        for (AchievementListener listener : listeners) {
            try {
                listener.onAchievementCollected(playerRef, def);
            } catch (Exception e) {
                System.err.println("[AchievementMod] Listener error for '"
                        + def.getId() + "': " + e.getMessage());
            }
        }
    }

    @Nonnull
    private PlayerAchievementData getOrCreate(@Nonnull PlayerRef playerRef,
                                              @Nonnull String achievementId) {
        return playerData
                .computeIfAbsent(playerKey(playerRef), k -> new HashMap<>())
                .computeIfAbsent(achievementId, k -> new PlayerAchievementData());
    }

    /**
     * Stable string key for the player map.
     * ⚠️ NEEDS VERIFICATION: PlayerRef.toString() may not be stable across
     * reconnects. Decompile PlayerRef — if it has getUniqueId() / getUUID()
     * / getName(), use that instead.
     */
    @Nonnull
    private String playerKey(@Nonnull PlayerRef playerRef) {
        return playerRef.toString();
    }
}