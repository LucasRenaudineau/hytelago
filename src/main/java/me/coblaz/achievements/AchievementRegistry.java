package me.coblaz.achievements;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.archipelago.ArchipelagoManager;
import me.coblaz.items.ItemReward;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class AchievementRegistry {

    private final String name;

    public AchievementRegistry(String name) {
        this.name = name;
    }

    // Internal State
    private final List<AchievementDefinition>                     definitions   = new ArrayList<>();
    private final Map<String, Map<String, PlayerAchievementData>> playerData    = new HashMap<>();
    private final List<AchievementListener>                       listeners     = new ArrayList<>();
    private final Set<String>                                     loadedPlayers = new HashSet<>();

    // Registration (called at plugin startup)

    public void registerAchievement(@Nonnull AchievementDefinition def) {
        boolean duplicate = definitions.stream().anyMatch(d -> d.getId().equals(def.getId()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "[AchievementMod] Duplicate achievement id: '" + def.getId() + "'"
            );
        }
        definitions.add(def);
    }

    public void addListener(@Nonnull AchievementListener listener) {
        listeners.add(listener);
    }

    // Data access

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

    // Game logic

    /**
     * Increases the progress counter for a player's achievement.
     * Does NOT automatically flip it to DONE; call refreshStatuses() for that
     * (the UI does this automatically on open).
     */
    public void incrementCount(@Nonnull PlayerRef playerRef,
                               @Nonnull String achievementId,
                               int amount) {
        getOrCreate(playerRef, achievementId).incrementCount(amount);
        savePlayer(playerRef);
    }

    /**
     * Directly overrides the status of a player's achievement.
     */
    public void setStatus(@Nonnull PlayerRef playerRef,
                          @Nonnull String achievementId,
                          @Nonnull AchievementStatus status) {
        getOrCreate(playerRef, achievementId).setStatus(status);
        savePlayer(playerRef);
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
        savePlayer(playerRef);
    }

    /**
     * Marks every DONE achievement as COLLECTED and fires all listeners.
     * Returns the list of achievements that were just collected (so the UI
     * can display them to the player).
     */
    @Nonnull
    public List<AchievementDefinition> collectDoneAchievements(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            boolean                     alsoCollectAlreadyCollected
    ) {
        return collectDoneAchievements(playerRef, ref, store, alsoCollectAlreadyCollected, null);
    }

    /**
     * As above, but only considers achievements whose id is in {@code visibleIds}.
     * Passing {@code null} considers every registered achievement. This keeps the
     * Collect button in sync with a filtered table (e.g. only the locations that
     * exist in the connected seed).
     */
    @Nonnull
    public List<AchievementDefinition> collectDoneAchievements(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            boolean                     alsoCollectAlreadyCollected,
            @Nullable Set<String>       visibleIds
    ) {
        List<AchievementDefinition> justCollected = new ArrayList<>();

        for (AchievementDefinition def : definitions) {
            if (visibleIds != null && !visibleIds.contains(def.getId())) continue;
            PlayerAchievementData data   = getOrCreate(playerRef, def.getId());
            AchievementStatus     status = data.getStatus();

            if (status == AchievementStatus.DONE) {
                data.setStatus(AchievementStatus.COLLECTED);
                justCollected.add(def);
                fireListeners(playerRef, def);
                giveRewardItems(ref, store, def);
            } else if (status == AchievementStatus.COLLECTED && alsoCollectAlreadyCollected) {
                justCollected.add(def);
                giveRewardItems(ref, store, def);
            }
        }

        if (!justCollected.isEmpty()) savePlayer(playerRef);
        return justCollected;
    }

    /**
     * /arch-collect command: sets count = neededCount and immediately collects.
     * Returns false if the id is unknown or already collected.
     */
    public boolean forceCollect(
            @Nonnull PlayerRef          playerRef,
            @Nonnull String             achievementId,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store
    ) {
        AchievementDefinition def = findDefinition(achievementId);
        if (def == null) return false;

        PlayerAchievementData data = getOrCreate(playerRef, achievementId);

        data.setCount(def.getNeededCount());
        data.setStatus(AchievementStatus.COLLECTED);
        fireListeners(playerRef, def);
        giveRewardItems(ref, store, def);
        savePlayer(playerRef);
        return true;
    }

    // Internals

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
        String key = playerKey(playerRef);
        loadIfNeeded(key);
        return playerData
                .computeIfAbsent(key, k -> new HashMap<>())
                .computeIfAbsent(achievementId, k -> new PlayerAchievementData());
    }

    private void loadIfNeeded(@Nonnull String key) {
        if (loadedPlayers.contains(key)) return;
        loadedPlayers.add(key);
        // Save files are scoped to the connected seed. With no seed (player not
        // connected) there is nothing to load — the table starts empty.
        String seedId = ArchipelagoManager.INSTANCE.getSeedId(key);
        if (seedId == null) return;
        Map<String, PlayerAchievementData> saved = AchievementSaveManager.load(seedId, name, key);
        if (!saved.isEmpty()) {
            playerData.put(key, saved);
        }
    }

    private void savePlayer(@Nonnull PlayerRef playerRef) {
        String key = playerKey(playerRef);
        // Only persist while connected to a seed; offline progress is not saved.
        String seedId = ArchipelagoManager.INSTANCE.getSeedId(key);
        if (seedId == null) return;

        Map<String, PlayerAchievementData> data = playerData.get(key);
        if (data == null) return;

        // The locations table is persisted with only the rows that exist in this
        // seed's slot data; the items registry is saved in full.
        Map<String, PlayerAchievementData> toSave = data;
        if ("locations".equals(name)) {
            toSave = new HashMap<>();
            for (Map.Entry<String, PlayerAchievementData> e : data.entrySet()) {
                if (ArchipelagoManager.INSTANCE.isLocationActive(key, e.getKey())) {
                    toSave.put(e.getKey(), e.getValue());
                }
            }
        }
        AchievementSaveManager.save(seedId, name, key, toSave);
    }

    /**
     * Drops the cached in-memory data and load flag for one player so the next
     * access reloads from the (now correct) per-seed file. Called on connect so a
     * reconnect to a different seed does not show the previous seed's progress.
     */
    public void invalidatePlayer(@Nonnull PlayerRef playerRef) {
        String key = playerKey(playerRef);
        playerData.remove(key);
        loadedPlayers.remove(key);
    }

    /**
     * Stable string key for the player map.
     */
    @Nonnull
    private String playerKey(@Nonnull PlayerRef playerRef) {
        return playerRef.getUuid().toString();
    }
    /** Directly sets the count for a player's achievement and saves. */
    public void setCount(@Nonnull PlayerRef playerRef,
                         @Nonnull String achievementId,
                         int count) {
        getOrCreate(playerRef, achievementId).setCount(count);
        savePlayer(playerRef);
    }

    private void giveRewardItems(
            @Nonnull Ref<EntityStore>      ref,
            @Nonnull Store<EntityStore>    store,
            @Nonnull AchievementDefinition def
    ) {
        if (def.getRewardItems().isEmpty()) return;

        InventoryComponent.Hotbar   hotbar   = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage  storage  = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());

        for (ItemReward reward : def.getRewardItems()) {
            ItemStack stack = new ItemStack(reward.itemId(), reward.quantity());
            boolean given   = false;

            if (hotbar   != null && hotbar.getInventory().canAddItemStack(stack)) {
                hotbar.getInventory().addItemStack(stack);
                given = true;
            } else if (storage  != null && storage.getInventory().canAddItemStack(stack)) {
                storage.getInventory().addItemStack(stack);
                given = true;
            } else if (backpack != null && backpack.getInventory().canAddItemStack(stack)) {
                backpack.getInventory().addItemStack(stack);
                given = true;
            }

            if (!given) {
                System.err.println("[AchievementMod] No inventory space to give '"
                        + reward.itemId() + "' x" + reward.quantity()
                        + " for achievement '" + def.getId() + "'");
            }
        }
    }
}