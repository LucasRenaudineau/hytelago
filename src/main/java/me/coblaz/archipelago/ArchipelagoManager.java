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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ArchipelagoManager {

    public static final ArchipelagoManager INSTANCE = new ArchipelagoManager();
    private ArchipelagoManager() {}

    // ── Pending actions: enqueued by WebSocket thread, drained by game thread ──
    @FunctionalInterface
    public interface PendingAction {
        void run(
                @Nonnull PlayerRef          playerRef,
                @Nonnull Ref<EntityStore>   ref,
                @Nonnull Store<EntityStore> store
        );
    }

    private final Map<String, ConcurrentLinkedQueue<PendingAction>> pendingByPlayer
            = new HashMap<>();

    // ── Concrete Client ────────────────────────────────────────────────────────
    private static final class HytaleAPClient extends Client {
        @Override public void onError(Exception ex) {
            System.err.println("[ArchipelagoMod] WebSocket ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
        @Override public void onClose(String reason, int attemptingReconnect) {
            System.out.printf("[ArchipelagoMod] WebSocket CLOSED  reason='%s'  reconnectAttempt=%d%n",
                    reason, attemptingReconnect);
        }
    }

    // Inside ArchipelagoManager — replace the anonymous Object with this:

    public static final class ItemEventListener {

        private final ConcurrentLinkedQueue<PendingAction> queue;
        private final AtomicInteger                        lastProcessed;
        private final String                               uuid;

        public ItemEventListener(
                ConcurrentLinkedQueue<PendingAction> queue,
                AtomicInteger                        lastProcessed,
                String                               uuid
        ) {
            this.queue         = queue;
            this.lastProcessed = lastProcessed;
            this.uuid          = uuid;
        }

        @ArchipelagoEventListener
        public void onReceive(ReceiveItemEvent event) {
            long   itemId   = event.getItemID();
            long   index    = event.getIndex();
            String itemName = event.getItemName();

            System.out.printf("[ArchipelagoMod][EVENT] ReceiveItemEvent  index=%d  itemId=%d  name='%s'  lastProcessed=%d%n",
                    index, itemId, itemName, lastProcessed.get());

            if (index <= lastProcessed.get()) {
                System.out.printf("[ArchipelagoMod]  → SKIPPED (already processed up to %d)%n",
                        lastProcessed.get());
                return;
            }

            if (itemId >= 1000 && itemId <= 1999) {
                MobSpawn spawn = MOB_TABLE.get(itemId);
                if (spawn != null) {
                    String mob   = spawn.mobName();
                    int    count = spawn.count();
                    System.out.printf("[ArchipelagoMod]  → Queuing MOB spawn: %dx %s%n", count, mob);
                    queue.add((pr, r, s) -> INSTANCE.spawnMob(pr, r, s, mob, count));
                } else {
                    System.err.printf("[ArchipelagoMod]  → UNKNOWN mob id: %d%n", itemId);
                }

            } else if (itemId >= 2000 && itemId <= 2999) {
                String base = TIER_TABLE.get(itemId);
                if (base != null) {
                    System.out.printf("[ArchipelagoMod]  → Queuing TIER increment: base='%s'%n", base);
                    queue.add((pr, r, s) -> INSTANCE.incrementTierAchievements(pr, base));
                } else {
                    System.err.printf("[ArchipelagoMod]  → UNKNOWN tier id: %d%n", itemId);
                }

            } else if (itemId >= 3000 && itemId <= 3999) {
                ItemGrant grant = LOOT_TABLE.get(itemId);
                if (grant != null) {
                    String gItem = grant.itemName();
                    int    qty   = grant.quantity();
                    System.out.printf("[ArchipelagoMod]  → Queuing LOOT grant: %dx '%s'%n", qty, gItem);
                    queue.add((pr, r, s) -> INSTANCE.giveItem(r, s, gItem, qty));
                } else {
                    System.err.printf("[ArchipelagoMod]  → UNKNOWN loot id: %d%n", itemId);
                }

            } else {
                System.err.printf("[ArchipelagoMod]  → Item id %d outside all known ranges!%n", itemId);
            }

            lastProcessed.set((int) index);
            ArchipelagoProgressSaveManager.saveLastIndex(uuid, (int) index);
            System.out.printf("[ArchipelagoMod]  → lastProcessed updated to %d%n", index);
        }
    }

    // ── Static item tables ────────────────────────────────────────────────────
    private static final Map<Long, MobSpawn>  MOB_TABLE  = new LinkedHashMap<>();
    private static final Map<Long, String>    TIER_TABLE = new LinkedHashMap<>();
    private static final Map<Long, ItemGrant> LOOT_TABLE = new LinkedHashMap<>();

    static {
        MOB_TABLE.put(1000L, new MobSpawn("Golem_Crystal_Earth",      1));
        MOB_TABLE.put(1001L, new MobSpawn("Golem_Firesteel",          1));
        MOB_TABLE.put(1002L, new MobSpawn("Skeleton_Frost_Archer",    2));
        MOB_TABLE.put(1003L, new MobSpawn("Outlander_Berserker",      1));
        MOB_TABLE.put(1004L, new MobSpawn("Eye_Void",                 1));
        MOB_TABLE.put(1005L, new MobSpawn("Zombie",                   2));
        MOB_TABLE.put(1006L, new MobSpawn("Scarak_Broodmother_Young", 1));

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

        LOOT_TABLE.put(3000L, new ItemGrant("Ore_Copper",                      10));
        LOOT_TABLE.put(3001L, new ItemGrant("Ore_Iron",                        10));
        LOOT_TABLE.put(3002L, new ItemGrant("Ore_Cobalt",                      10));
        LOOT_TABLE.put(3003L, new ItemGrant("Ore_Gold",                        10));
        LOOT_TABLE.put(3004L, new ItemGrant("Ore_Silver",                      10));
        LOOT_TABLE.put(3005L, new ItemGrant("Ore_Thorium",                     10));
        LOOT_TABLE.put(3006L, new ItemGrant("Ingredient_Hide_Soft",            10));
        LOOT_TABLE.put(3007L, new ItemGrant("Ingredient_Hide_Light",           10));
        LOOT_TABLE.put(3008L, new ItemGrant("Ingredient_Hide_Medium",          10));
        LOOT_TABLE.put(3009L, new ItemGrant("Ingredient_Hide_Heavy",           10));
        LOOT_TABLE.put(3010L, new ItemGrant("Ingredient_Chitin_Sturdy",        10));
        LOOT_TABLE.put(3011L, new ItemGrant("Ingredient_Poop",                  1));
        LOOT_TABLE.put(3012L, new ItemGrant("Flamethrower_Goblin",              1));
        LOOT_TABLE.put(3013L, new ItemGrant("Teleporter",                       3));
        LOOT_TABLE.put(3014L, new ItemGrant("Armor_Adamantite_Legs",            1));
        LOOT_TABLE.put(3015L, new ItemGrant("Armor_Adamantite_Chest",           1));
        LOOT_TABLE.put(3016L, new ItemGrant("Armor_Adamantite_Head",            1));
        LOOT_TABLE.put(3017L, new ItemGrant("Armor_Adamantite_Hands",           1));
        LOOT_TABLE.put(3018L, new ItemGrant("Weapon_Shield_Adamantite",         1));
        LOOT_TABLE.put(3019L, new ItemGrant("Weapon_Axe_Cobalt",                1));
        LOOT_TABLE.put(3020L, new ItemGrant("Weapon_Longsword_Copper",          1));
        LOOT_TABLE.put(3021L, new ItemGrant("Weapon_Shortbow_Combat",           1));
        LOOT_TABLE.put(3022L, new ItemGrant("Weapon_Daggers_Bone",              1));
        LOOT_TABLE.put(3023L, new ItemGrant("Weapon_Arrow_Clearshot",          30));
        LOOT_TABLE.put(3024L, new ItemGrant("Weapon_Deployable_Healing_Totem",  1));
        LOOT_TABLE.put(3025L, new ItemGrant("Tool_Repair_Kit_Iron",                       2));
    }

    // ── Per-player connection state ───────────────────────────────────────────
    private final Map<String, PlayerAPState> playerStates = new HashMap<>();

    // ── connect() ─────────────────────────────────────────────────────────────
    public void connect(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String             ip,
            int                         port,
            @Nonnull String             slotName
    ) {
        String uuid = playerRef.getUuid().toString();
        System.out.printf("[ArchipelagoMod] connect() called  player=%s  slot=%s  host=%s:%d%n",
                uuid, slotName, ip, port);

        PlayerAPState old = playerStates.remove(uuid);
        if (old != null) {
            System.out.println("[ArchipelagoMod] Closing previous connection for " + uuid);
            old.client().close();
        }

        pendingByPlayer.computeIfAbsent(uuid, k -> new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<PendingAction> queue = pendingByPlayer.get(uuid);

        int saved = ArchipelagoProgressSaveManager.loadLastIndex(uuid);
        AtomicInteger lastProcessed = new AtomicInteger(saved);
        System.out.printf("[ArchipelagoMod] Resuming from saved index %d%n", saved);

        HytaleAPClient client = new HytaleAPClient();
        client.setGame("Hytale");
        client.setName(slotName);
        client.setPassword("");   // set explicitly even if empty; some servers require it

        // ── CRITICAL: tell the server to send us items ─────────────────────
        // 0b001 = receive items from other worlds
        // 0b010 = starting inventory
        // 0b100 = items received while offline
        // Without this, the server may connect us but never send ReceivedItems.
        try {
            // Try the most common method names — check your library's decompilation
            // and use whichever one exists:
            client.setItemsHandlingFlags(0b111);   // preferred: 7
            System.out.println("[ArchipelagoMod] items_handling set to 0b111 (7)");
        } catch (NoSuchMethodError | AbstractMethodError e) {
            // If setItemsHandling doesn't exist, look for it under a different name
            System.err.println("[ArchipelagoMod] WARNING: setItemsHandling not found on Client! " +
                    "Items will NOT be received. Decompile Client.class to find the correct setter.");
        }

        ItemEventListener itemListener = new ItemEventListener(queue, lastProcessed, uuid);
        client.getEventManager().registerListener(itemListener);
        System.out.println("[ArchipelagoMod] Listener registered on event manager");

        playerStates.put(uuid, new PlayerAPState(client, lastProcessed, ref, store, itemListener));

        try {
            client.connect(ip + ":" + port);
            System.out.printf("[ArchipelagoMod] connect() returned (WebSocket handshake initiated) — slot=%s%n", slotName);
        } catch (URISyntaxException e) {
            playerStates.remove(uuid);
            throw new RuntimeException("[ArchipelagoMod] Bad URI: " + e.getMessage(), e);
        }
    }

    // ── tick() ────────────────────────────────────────────────────────────────
    public void tick(
            @Nonnull PlayerRef          playerRef,
            @Nonnull Store<EntityStore> freshStore
    ) {
        String uuid = playerRef.getUuid().toString();
        ConcurrentLinkedQueue<PendingAction> queue = pendingByPlayer.get(uuid);
        if (queue == null || queue.isEmpty()) return;

        PlayerAPState state = playerStates.get(uuid);
        if (state == null) return;

        Ref<EntityStore> ref = state.ref();

        PendingAction action;
        while ((action = queue.poll()) != null) {
            try {
                action.run(playerRef, ref, freshStore);
            } catch (Exception ex) {
                System.err.printf("[ArchipelagoMod] Error executing queued action for %s: %s%n",
                        uuid, ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public boolean isConnected(@Nonnull PlayerRef playerRef) {
        PlayerAPState state = playerStates.get(playerRef.getUuid().toString());
        return state != null && state.client().isConnected();
    }

    public void disconnect(@Nonnull PlayerRef playerRef) {
        String uuid = playerRef.getUuid().toString();
        PlayerAPState state = playerStates.remove(uuid);
        if (state != null) state.client().close();
        pendingByPlayer.remove(uuid);
    }

    // ── Dispatchers ───────────────────────────────────────────────────────────
    private void spawnMob(
            @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store, @Nonnull String mobName, int count
    ) {
        System.out.printf("[ArchipelagoMod] TODO spawn %dx %s near %s%n",
                count, mobName, playerRef.getUuid());
    }

    private void incrementTierAchievements(@Nonnull PlayerRef playerRef, @Nonnull String baseName) {
        List<String> achIds = ItemsAchievements.ALL.stream()
                .filter(e -> e.itemId().toLowerCase().startsWith(baseName.toLowerCase()))
                .map(ItemsAchievements.Entry::achievementId)
                .collect(Collectors.toList());

        if (achIds.isEmpty()) {
            System.err.println("[ArchipelagoMod] No achievements for base '" + baseName + "'");
            return;
        }
        for (String achId : achIds) Registries.ITEMS.incrementCount(playerRef, achId, 1);
        Registries.ITEMS.refreshStatuses(playerRef);
        System.out.printf("[ArchipelagoMod] Incremented tier achievements for '%s': %s%n", baseName, achIds);
    }

    private void giveItem(
            @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull String itemName, int quantity
    ) {
        ItemStack stack = new ItemStack(itemName, quantity);
        InventoryComponent.Hotbar   hotbar   = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage  storage  = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());

        if      (hotbar   != null && hotbar.getInventory().canAddItemStack(stack))
            hotbar.getInventory().addItemStack(stack);
        else if (storage  != null && storage.getInventory().canAddItemStack(stack))
            storage.getInventory().addItemStack(stack);
        else if (backpack != null && backpack.getInventory().canAddItemStack(stack))
            backpack.getInventory().addItemStack(stack);
        else
            System.err.printf("[ArchipelagoMod] No inventory space for %s x%d%n", itemName, quantity);
    }
    // ── Send a completed location check to the AP server ─────────────────────────
    /**
     * Called when a player collects an achievement that maps to an Archipelago
     * location. Looks up the numeric location ID and sends it to the server.
     *
     * Safe to call from the game thread (no queuing needed — the AP client's
     * WebSocket send is thread-safe).
     */
    public void sendLocationCheck(
            @Nonnull PlayerRef playerRef,
            @Nonnull String    achievementId
    ) {
        // ── 1. Resolve numeric location ID ────────────────────────────────────
        Long locationId = ArchipelagoLocationMap.getLocationId(achievementId);
        if (locationId == null) {
            System.out.printf(
                    "[ArchipelagoMod] sendLocationCheck: achievement '%s' has no AP location ID — skipping%n",
                    achievementId);
            return;
        }

        // ── 2. Check that the player is connected ─────────────────────────────
        String        uuid  = playerRef.getUuid().toString();
        PlayerAPState state = playerStates.get(uuid);
        if (state == null || !state.client().isConnected()) {
            System.out.printf(
                    "[ArchipelagoMod] sendLocationCheck: player %s is not connected — check for '%s' (id=%d) NOT sent%n",
                    uuid, achievementId, locationId);
            return;
        }

        // ── 3. Send ───────────────────────────────────────────────────────────
        System.out.printf(
                "[ArchipelagoMod] Sending location check: '%s' → location id %d%n",
                achievementId, locationId);
        state.client().checkLocation(locationId);

        System.out.printf(
                "[ArchipelagoMod] Location check sent successfully: '%s' (id=%d) for player %s%n",
                achievementId, locationId, uuid);
    }
}