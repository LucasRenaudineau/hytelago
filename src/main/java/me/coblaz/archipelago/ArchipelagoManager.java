package me.coblaz.archipelago;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.archipelagomw.Client;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ReceiveItemEvent;
import me.coblaz.achievements.Registries;
import me.coblaz.items.ItemsAchievements;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Singleton that owns one Client per connected player.
 *
 * Item-ID ranges:
 *   1000–1999  →  spawn mob near player  (trailing digit = count)
 *   2000–2999  →  increment tier-upgrader achievement counter
 *   3000–3999  →  give item to player    (trailing digit = quantity)
 *
 * The last processed item index is persisted via {@link ArchipelagoProgressSaveManager}
 * so duplicate delivery is skipped on reconnect.
 */
public final class ArchipelagoManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    public static final ArchipelagoManager INSTANCE = new ArchipelagoManager();
    private ArchipelagoManager() {}

    // ── Concrete Client subclass ───────────────────────────────────────────────
    private static final class HytaleAPClient extends Client {
        @Override
        public void onError(Exception ex) {
            System.err.println("[ArchipelagoMod] WebSocket error: " + ex.getMessage());
        }

        @Override
        public void onClose(String reason, int attemptingReconnect) {
            System.out.printf("[ArchipelagoMod] Connection closed: %s (reconnect attempt %d)%n",
                    reason, attemptingReconnect);
        }
    }

    // ── Static item tables ────────────────────────────────────────────────────

    /** 1000–1999 : mob name → (role name, spawn count) */
    private static final Map<Long, MobSpawn> MOB_TABLE = new LinkedHashMap<>();

    /** 2000–2999 : AP item id → base name used for prefix match in ItemsAchievements */
    private static final Map<Long, String> TIER_TABLE = new LinkedHashMap<>();

    /** 3000–3999 : AP item id → (Hytale item id, quantity to give) */
    private static final Map<Long, ItemGrant> LOOT_TABLE = new LinkedHashMap<>();

    static {
        // ── Mob spawns ─────────────────────────────────────────────────────────
        MOB_TABLE.put(1000L, new MobSpawn("Golem_Crystal_Earth",        1));
        MOB_TABLE.put(1001L, new MobSpawn("Golem_Firesteel",            1));
        MOB_TABLE.put(1002L, new MobSpawn("Skeleton_Frost_Archer",      2));
        MOB_TABLE.put(1003L, new MobSpawn("Outlander_Berserker",        1));
        MOB_TABLE.put(1004L, new MobSpawn("Eye_Void",                   1));
        MOB_TABLE.put(1005L, new MobSpawn("Zombie",                     2));
        MOB_TABLE.put(1006L, new MobSpawn("Scarak_Broodmother_Young",   1));

        // ── Tier upgraders ─────────────────────────────────────────────────────
        TIER_TABLE.put(2000L, "Progressive_Armorer");
        TIER_TABLE.put(2001L, "Progressive_Backpack");
        TIER_TABLE.put(2002L, "Progressive_Blacksmith");
        TIER_TABLE.put(2003L, "Campfire");
        TIER_TABLE.put(2004L, "Chef_Stove");
        TIER_TABLE.put(2005L, "Progressive_Farmer");
        TIER_TABLE.put(2006L, "Progressive_Furnace");
        TIER_TABLE.put(2007L, "Salvager");
        TIER_TABLE.put(2008L, "Progressive_Tanning");
        TIER_TABLE.put(2009L, "Progressive_Workbench");

        // ── Random loot ────────────────────────────────────────────────────────
        LOOT_TABLE.put(3000L, new ItemGrant("Ore_Copper",                       10));
        LOOT_TABLE.put(3001L, new ItemGrant("Ore_Iron",                         10));
        LOOT_TABLE.put(3002L, new ItemGrant("Ore_Cobalt",                       10));
        LOOT_TABLE.put(3003L, new ItemGrant("Ore_Gold",                         10));
        LOOT_TABLE.put(3004L, new ItemGrant("Ore_Silver",                       10));
        LOOT_TABLE.put(3005L, new ItemGrant("Ore_Thorium",                      10));
        LOOT_TABLE.put(3006L, new ItemGrant("Ingredient_Hide_Soft",             10));
        LOOT_TABLE.put(3007L, new ItemGrant("Ingredient_Hide_Light",            10));
        LOOT_TABLE.put(3008L, new ItemGrant("Ingredient_Hide_Medium",           10));
        LOOT_TABLE.put(3009L, new ItemGrant("Ingredient_Hide_Heavy",            10));
        LOOT_TABLE.put(3010L, new ItemGrant("Ingredient_Chitin_Sturdy",         10));
        LOOT_TABLE.put(3011L, new ItemGrant("Ingredient_Poop",                   1));
        LOOT_TABLE.put(3012L, new ItemGrant("Flamethrower_Goblin",               1));
        LOOT_TABLE.put(3013L, new ItemGrant("Teleporter",                        3));
        LOOT_TABLE.put(3014L, new ItemGrant("Armor_Adamantite_Legs",             1));
        LOOT_TABLE.put(3015L, new ItemGrant("Armor_Adamantite_Chest",            1));
        LOOT_TABLE.put(3016L, new ItemGrant("Armor_Adamantite_Head",             1));
        LOOT_TABLE.put(3017L, new ItemGrant("Armor_Adamantite_Hands",            1));
        LOOT_TABLE.put(3018L, new ItemGrant("Weapon_Shield_Adamantite",          1));
        LOOT_TABLE.put(3019L, new ItemGrant("Weapon_Axe_Cobalt",                 1));
        LOOT_TABLE.put(3020L, new ItemGrant("Weapon_Longsword_Copper",           1));
        LOOT_TABLE.put(3021L, new ItemGrant("Weapon_Shortbow_Combat",            1));
        LOOT_TABLE.put(3022L, new ItemGrant("Weapon_Daggers_Bone",               1));
        LOOT_TABLE.put(3023L, new ItemGrant("Weapon_Arrow_Clearshot",           30));
        LOOT_TABLE.put(3024L, new ItemGrant("Weapon_Deployable_Healing_Totem",   1));
        LOOT_TABLE.put(3025L, new ItemGrant("Repair_Kit",                        2));
    }

    // ── Per-player connection state ───────────────────────────────────────────

    /** Player UUID (string) → active connection + inventory context */
    private final Map<String, PlayerAPState> playerStates = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Opens a new Archipelago connection for {@code playerRef}.
     * Called from {@link me.coblaz.commands.ArchConnectCommand}.
     */
    public void connect(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String             ip,
            int                         port,
            @Nonnull String             slotName
    ) {
        String uuid = playerRef.getUuid().toString();

        // Close any previous connection for this player cleanly
        PlayerAPState old = playerStates.remove(uuid);
        if (old != null) old.client().close();

        // Determine resume point: skip every item at or below this index
        int saved = ArchipelagoProgressSaveManager.loadLastIndex(uuid);
        AtomicInteger lastProcessed = new AtomicInteger(saved);

        HytaleAPClient client = new HytaleAPClient();
        client.setGame("Hytale");
        client.setName(slotName);

        // Capture finals for the listener
        final String        capturedUuid = uuid;
        final AtomicInteger captured     = lastProcessed;

        client.getEventManager().registerListener(new Object() {
            @ArchipelagoEventListener
            public void onReceive(ReceiveItemEvent event) {
                int index = (int) event.getIndex();

                // Guard: ignore items already delivered in a previous session
                if (index <= captured.get()) return;

                long   itemId   = event.getItemID();
                String itemName = event.getItemName();

                System.out.printf("[ArchipelagoMod] Received item #%d  id=%d  name=%s%n",
                        index, itemId, itemName);

                if (itemId >= 1000 && itemId <= 1999) {
                    MobSpawn spawn = MOB_TABLE.get(itemId);
                    if (spawn != null) spawnMob(playerRef, ref, store, spawn.mobName(), spawn.count());
                    else System.err.println("[ArchipelagoMod] Unknown mob item id: " + itemId);

                } else if (itemId >= 2000 && itemId <= 2999) {
                    String base = TIER_TABLE.get(itemId);
                    if (base != null) incrementTierAchievements(playerRef, base);
                    else System.err.println("[ArchipelagoMod] Unknown tier item id: " + itemId);

                } else if (itemId >= 3000 && itemId <= 3999) {
                    ItemGrant grant = LOOT_TABLE.get(itemId);
                    if (grant != null) giveItem(ref, store, grant.itemName(), grant.quantity());
                    else System.err.println("[ArchipelagoMod] Unknown loot item id: " + itemId);
                }

                captured.set(index);
                ArchipelagoProgressSaveManager.saveLastIndex(capturedUuid, index);
            }
        });

        // Put the state before connecting to avoid a race if items arrive instantly
        playerStates.put(uuid, new PlayerAPState(client, lastProcessed, ref, store));

        try {
            client.connect(ip + ":" + port);
            System.out.printf("[ArchipelagoMod] %s connected to %s:%d (resuming from index %d)%n",
                    slotName, ip, port, saved);
        } catch (URISyntaxException e) {
            playerStates.remove(uuid);
            System.err.println("[ArchipelagoMod] Bad address '" + ip + ":" + port + "': " + e.getMessage());
            throw new RuntimeException("[ArchipelagoMod] Connection failed: " + e.getMessage(), e);
        }
    }

    /** Returns true if the given player currently has an open AP connection. */
    public boolean isConnected(@Nonnull PlayerRef playerRef) {
        PlayerAPState state = playerStates.get(playerRef.getUuid().toString());
        return state != null && state.client().isConnected();
    }

    /** Closes the AP connection for a player (e.g. on server logout). */
    public void disconnect(@Nonnull PlayerRef playerRef) {
        PlayerAPState state = playerStates.remove(playerRef.getUuid().toString());
        if (state != null) state.client().close();
    }

    // ── Dispatchers ───────────────────────────────────────────────────────────

    /**
     * Spawns {@code count} instances of {@code mobName} near the player.
     * TODO: fill in using your ArchSpawnCommand logic / Hytale NPC-spawn API.
     */
    private void spawnMob(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String             mobName,
            int                         count
    ) {
        System.out.printf("[ArchipelagoMod] TODO spawn %dx %s near %s%n",
                count, mobName, playerRef.getUuid());
    }

    /**
     * Increments the counter on every ItemsAchievements entry whose itemId
     * starts with {@code baseName} (case-insensitive), then refreshes statuses.
     */
    private void incrementTierAchievements(
            @Nonnull PlayerRef playerRef,
            @Nonnull String    baseName
    ) {
        List<String> achIds = ItemsAchievements.ALL.stream()
                .filter(e -> e.itemId().toLowerCase().startsWith(baseName.toLowerCase()))
                .map(ItemsAchievements.Entry::achievementId)
                .collect(Collectors.toList());

        if (achIds.isEmpty()) {
            System.err.println("[ArchipelagoMod] No achievements found for base '" + baseName + "'");
            return;
        }

        for (String achId : achIds) {
            Registries.ITEMS.incrementCount(playerRef, achId, 1);
        }
        Registries.ITEMS.refreshStatuses(playerRef);

        System.out.printf("[ArchipelagoMod] Incremented tier achievements for '%s': %s%n",
                baseName, achIds);
    }

    /**
     * Gives {@code quantity} of {@code itemName} to the player,
     * filling hotbar → storage → backpack in that order.
     */
    private void giveItem(
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String             itemName,
            int                         quantity
    ) {
        ItemStack stack = new ItemStack(itemName, quantity);

        InventoryComponent.Hotbar   hotbar   = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage  storage  = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());

        if (hotbar != null && hotbar.getInventory().canAddItemStack(stack)) {
            hotbar.getInventory().addItemStack(stack);
        } else if (storage != null && storage.getInventory().canAddItemStack(stack)) {
            storage.getInventory().addItemStack(stack);
        } else if (backpack != null && backpack.getInventory().canAddItemStack(stack)) {
            backpack.getInventory().addItemStack(stack);
        } else {
            System.err.printf("[ArchipelagoMod] No inventory space for %s x%d%n", itemName, quantity);
        }
    }
}