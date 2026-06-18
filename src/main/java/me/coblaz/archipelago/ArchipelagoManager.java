package me.coblaz.archipelago;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import io.github.archipelagomw.Client;
import io.github.archipelagomw.Print.APPrint;
import io.github.archipelagomw.bounce.DeathLinkHandler;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ConnectionResultEvent;
import io.github.archipelagomw.events.DeathLinkEvent;
import io.github.archipelagomw.events.PrintJSONEvent;
import io.github.archipelagomw.events.ReceiveItemEvent;
import io.github.archipelagomw.network.ConnectionResult;
import me.coblaz.achievements.Registries;
import me.coblaz.items.ItemsAchievements;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ArchipelagoManager {

    public static final ArchipelagoManager INSTANCE = new ArchipelagoManager();
    private ArchipelagoManager() {}

    // Pending actions: enqueued by WebSocket thread, drained by game thread
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

    // Concrete Client
    private static final class HytaleAPClient extends Client {

        private final ConcurrentLinkedQueue<PendingAction> feedbackQueue;
        private final String                               slotName;

        // The connect() call only opens a WebSocket; the slot is not actually
        // joined until the server answers with a ConnectionResultEvent (or the
        // socket closes on failure). This flag marks that the server has given
        // a verdict, so a later socket close doesn't double-report a failure.
        private volatile boolean resultReported = false;

        // True only between a successful ConnectionResultEvent and the socket
        // closing. The base Client.isConnected() merely reports that the
        // WebSocket is open, which happens before the slot is confirmed — so we
        // must NOT treat that as "connected" for gameplay purposes.
        private volatile boolean slotConnected = false;

        // Set when we close this client on purpose (superseding it with a new
        // connection, or /arch-disconnect). Such a clean close must not be
        // reported to the player as a connection failure.
        private volatile boolean intentionalClose = false;

        HytaleAPClient(ConcurrentLinkedQueue<PendingAction> feedbackQueue, String slotName) {
            this.feedbackQueue = feedbackQueue;
            this.slotName      = slotName;
        }

        /** True only once the AP server has accepted the slot and the socket is still open. */
        boolean isSlotConnected() {
            return slotConnected && isConnected();
        }

        /** Close without surfacing a "Connection failed" message to the player. */
        void closeSilently() {
            intentionalClose = true;
            close();
        }

        @Override public void onError(Exception ex) {
            System.err.println("[ArchipelagoMod] WebSocket ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }

        @Override public void onClose(String reason, int attemptingReconnect) {
            System.out.printf("[ArchipelagoMod] WebSocket CLOSED  reason='%s'  reconnectAttempt=%d%n",
                    reason, attemptingReconnect);
            boolean wasSlotConnected = slotConnected;
            slotConnected = false;

            // A close we triggered ourselves (superseded by a new connection, or
            // /arch-disconnect) is never a failure — stay silent.
            if (intentionalClose) return;

            // Ignore reconnect-attempt closes, which carry a non-zero delay.
            if (attemptingReconnect != 0) return;

            if (resultReported) {
                // We were genuinely connected and the link dropped afterwards.
                // Only tell the player if the slot had actually been joined.
                if (wasSlotConnected) {
                    showTitle("Archipelago", "Disconnected from the server.");
                }
            } else {
                // Socket closed before the server ever confirmed the slot
                // (e.g. host unreachable or refused).
                resultReported = true;
                showTitle("Connection failed",
                        (reason == null || reason.isBlank())
                                ? "Could not reach the Archipelago server."
                                : reason);
            }
        }

        private void showTitle(String title, String subtitle) {
            // Defer to the game thread via the per-player action queue.
            feedbackQueue.add((pr, r, s) -> EventTitleUtil.showEventTitleToPlayer(
                    pr, Message.raw(title), Message.raw(subtitle), true));
        }
    }

    /**
     * Listens for the AP server's answer to our Connect packet — the real
     * success/failure signal (not connect() returning).
     *
     * This MUST be a public top-level/nested listener object, NOT the
     * HytaleAPClient itself: EventManager dispatches with reflective
     * method.invoke() and never calls setAccessible(true), so a listener method
     * declared on a non-public class (like the private HytaleAPClient) throws
     * IllegalAccessException and is silently dropped — the connection result
     * would never be received. The Minecraft mod registers a separate public
     * APConnectEvents object for the same reason.
     */
    public static final class ConnectionResultListener {

        private final HytaleAPClient                       client;
        private final ConcurrentLinkedQueue<PendingAction> feedbackQueue;
        private final String                               slotName;
        private final String                               uuid;
        private final AtomicInteger                        lastProcessed;

        public ConnectionResultListener(
                HytaleAPClient                       client,
                ConcurrentLinkedQueue<PendingAction> feedbackQueue,
                String                               slotName,
                String                               uuid,
                AtomicInteger                        lastProcessed
        ) {
            this.client        = client;
            this.feedbackQueue = feedbackQueue;
            this.slotName      = slotName;
            this.uuid          = uuid;
            this.lastProcessed = lastProcessed;
        }

        @ArchipelagoEventListener
        public void onConnectionResult(ConnectionResultEvent event) {
            client.resultReported = true;
            ConnectionResult result = event.getResult();
            System.out.printf("[ArchipelagoMod][EVENT] ConnectionResultEvent  result=%s%n", result);
            if (result == ConnectionResult.Success) {
                client.slotConnected = true;

                // Build the location/item tables from this seed's slot data, and
                // apply the Death Link setting the player generated with.
                HytaleSlotData slot = null;
                try {
                    slot = event.getSlotData(HytaleSlotData.class);
                } catch (Exception ex) {
                    System.err.println("[ArchipelagoMod] Could not parse slot data: " + ex.getMessage());
                }

                // Identify this seed so all saved files (tables + item index) are
                // scoped to this specific game. Set BEFORE loading the index so the
                // per-seed path resolves correctly.
                INSTANCE.onSlotConnected(uuid, slot, event.getSeedName(), slotName);

                // Now the seed is known, resume the item index from this seed's
                // file. Safe on the WebSocket thread: AtomicInteger is thread-safe
                // and this runs before any ReceiveItemEvent on the same thread.
                String seedId = INSTANCE.getSeedId(uuid);
                int saved = ArchipelagoProgressSaveManager.loadLastIndex(seedId, uuid);
                lastProcessed.set(saved);
                System.out.printf("[ArchipelagoMod] Resuming from saved index %d (seed=%s)%n", saved, seedId);

                // Drop any in-memory achievement data from a previous seed so the
                // table reloads from this seed's file. Done on the game thread.
                feedbackQueue.add((pr, r, s) -> {
                    Registries.LOCATIONS.invalidatePlayer(pr);
                    Registries.ITEMS.invalidatePlayer(pr);
                });

                if (slot != null) {
                    client.setDeathLinkEnabled(slot.hasDeathLink());
                    System.out.printf("[ArchipelagoMod] DeathLink %s from slot data for %s%n",
                            slot.hasDeathLink() ? "ENABLED" : "DISABLED", slotName);
                }

                showTitle("Archipelago", "Connected as " + slotName);
            } else {
                showTitle("Connection failed", describe(result));
            }
        }

        private void showTitle(String title, String subtitle) {
            feedbackQueue.add((pr, r, s) -> EventTitleUtil.showEventTitleToPlayer(
                    pr, Message.raw(title), Message.raw(subtitle), true));
        }

        private static String describe(ConnectionResult result) {
            return switch (result) {
                case Success             -> "Connected.";
                case InvalidSlot         -> "Unknown slot name.";
                case InvalidGame         -> "Slot is for a different game.";
                case SlotAlreadyTaken    -> "That slot is already taken.";
                case IncompatibleVersion -> "Incompatible Archipelago version.";
                case InvalidPassword     -> "Invalid password.";
            };
        }
    }

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
                System.out.printf("[ArchipelagoMod] -> SKIPPED (already processed up to %d)%n",
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
            ArchipelagoProgressSaveManager.saveLastIndex(INSTANCE.getSeedId(uuid), uuid, (int) index);
            System.out.printf("[ArchipelagoMod]  → lastProcessed updated to %d%n", index);
        }
    }

    /**
     * Listens for DeathLink bounce packets from the AP server. When another
     * player dies, this queues an action to kill the local player in Hytale.
     */
    public static final class DeathLinkEventListener {

        private final ConcurrentLinkedQueue<PendingAction> queue;

        public DeathLinkEventListener(ConcurrentLinkedQueue<PendingAction> queue) {
            this.queue = queue;
        }

        @ArchipelagoEventListener
        public void onDeathLink(DeathLinkEvent event) {
            System.out.printf("[ArchipelagoMod][EVENT] DeathLinkEvent  source='%s'  cause='%s'%n",
                    event.source, event.cause);
            queue.add((pr, r, s) -> INSTANCE.killPlayer(pr, r, s));
        }
    }

    /**
     * Listens for PrintJSON packets — every human-readable message the AP server
     * broadcasts (item sends, hints, joins/parts, chat, goals, countdowns, ...).
     * The Java-Client's WebSocket has already resolved player/item/location IDs
     * into names before firing this event, so joining the parts yields plain
     * English. The text is shown in the player's chat box (like /arch-help),
     * not as an on-screen event title.
     *
     * Must be a public class — see ConnectionResultListener for why.
     */
    public static final class PrintJSONListener {

        private final ConcurrentLinkedQueue<PendingAction> queue;

        public PrintJSONListener(ConcurrentLinkedQueue<PendingAction> queue) {
            this.queue = queue;
        }

        @ArchipelagoEventListener
        public void onPrintJSON(PrintJSONEvent event) {
            APPrint print = event.apPrint;
            if (print == null) return;

            // Parts already carry resolved names; fall back to the raw message
            // field for packets that have no parts.
            String text = (print.parts != null) ? print.getPlainText() : print.message;
            if (text == null || text.isBlank()) return;

            System.out.printf("[ArchipelagoMod][EVENT] PrintJSONEvent  type=%s  text='%s'%n",
                    event.type, text);

            String line = "[Archipelago] " + text;
            queue.add((pr, r, s) -> pr.sendMessage(Message.raw(line)));
        }
    }

    // Static item table
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
        MOB_TABLE.put(1007L, new MobSpawn("Scarak_Seeker",            1));
        MOB_TABLE.put(1008L, new MobSpawn("Yeti",                     1));

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
        LOOT_TABLE.put(3025L, new ItemGrant("Tool_Repair_Kit_Iron",             2));
        LOOT_TABLE.put(3026L, new ItemGrant("Plant_Crop_Carrot_Item",           5));
        LOOT_TABLE.put(3027L, new ItemGrant("Plant_Seeds_Corn",                 1));
        LOOT_TABLE.put(3028L, new ItemGrant("Plant_Cactus_1",                   1));
        LOOT_TABLE.put(3029L, new ItemGrant("Ingredient_Charcoal",             10));
        LOOT_TABLE.put(3030L, new ItemGrant("Furniture_Village_Chest_Small",    2));
        LOOT_TABLE.put(3031L, new ItemGrant("Plant_Crop_Wheat_Block",           3));
        LOOT_TABLE.put(3032L, new ItemGrant("Food_Bread",                       5));
    }

    // Per-player connection state
    private final Map<String, PlayerAPState> playerStates = new HashMap<>();

    // Per-player slot data and the active location-id table built from it on
    // connection. Populated by onSlotConnected(); read by sendLocationCheck().
    private final Map<String, HytaleSlotData> slotDataByPlayer       = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>>      activeLocationsByPlayer = new ConcurrentHashMap<>();

    // Per-player identity of the connected seed (uuid -> seedId), used to scope
    // all on-disk save files to one specific game so two seeds never share files.
    private final Map<String, String>         seedIdByPlayer          = new ConcurrentHashMap<>();

    // UUIDs of players whose next death was caused by an incoming DeathLink.
    // Used to avoid bouncing that death straight back to the server (loop).
    private final Set<String> deathLinkSuppress = ConcurrentHashMap.newKeySet();

    /**
     * Called when the AP server confirms the slot. Stores the slot data and
     * builds the active location table that defines which achievements are real
     * checks for this seed. A {@code null} slot data (older seeds that send
     * none) clears any previous table, falling back to "every location is live".
     */
    public void onSlotConnected(@Nonnull String uuid,
                                @javax.annotation.Nullable HytaleSlotData slot,
                                @javax.annotation.Nullable String seedName,
                                @Nonnull String slotName) {
        // Scope all save files to this game regardless of whether slot data was
        // sent. A missing seed name (very old servers) falls back to the slot name.
        String seedId = sanitizeSeedId((seedName == null || seedName.isBlank() ? "unknown" : seedName)
                + "_" + slotName);
        seedIdByPlayer.put(uuid, seedId);

        if (slot == null) {
            slotDataByPlayer.remove(uuid);
            activeLocationsByPlayer.remove(uuid);
            System.out.println("[ArchipelagoMod] No slot data received — using full location table.");
            return;
        }
        Set<Long> active = ArchipelagoLocationMap.buildActiveLocationIds(slot);
        slotDataByPlayer.put(uuid, slot);
        activeLocationsByPlayer.put(uuid, active);
        System.out.printf(
                "[ArchipelagoMod] Built tables from slot data for %s: %d active locations "
                        + "(death_link=%b traps=%b death_locations=%b memories=%b[max=%d every=%d] regions=%b)%n",
                uuid, active.size(), slot.hasDeathLink(), slot.hasTraps(), slot.hasDeathLocations(),
                slot.hasMemories(), slot.memories_max, slot.memories_every, slot.hasRegions());
    }

    private static String sanitizeSeedId(@Nonnull String raw) {
        return raw.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    // connect()
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
            closeClient(old.client());
        }

        pendingByPlayer.computeIfAbsent(uuid, k -> new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<PendingAction> queue = pendingByPlayer.get(uuid);

        // The saved item index is per-seed and the seed is unknown until the
        // server confirms the slot, so start neutral here; ConnectionResultListener
        // loads the real value once the seed id is known.
        AtomicInteger lastProcessed = new AtomicInteger(-1);

        HytaleAPClient client = new HytaleAPClient(queue, slotName);
        client.setGame("Hytale");
        client.setName(slotName);
        client.setPassword("");   // set explicitly even if empty; some servers require it

        // CRITICAL: tell the server to send us items
        // 0b001 = receive items from other worlds
        // 0b010 = starting inventory
        // 0b100 = items received while offline
        try {
            client.setItemsHandlingFlags(0b111);   // preferred: 7
            System.out.println("[ArchipelagoMod] items_handling set to 0b111 (7)");
        } catch (NoSuchMethodError | AbstractMethodError e) {
            System.err.println("[ArchipelagoMod] WARNING: setItemsHandling not found on Client! ");
        }

        ItemEventListener itemListener = new ItemEventListener(queue, lastProcessed, uuid);
        client.getEventManager().registerListener(itemListener);

        DeathLinkEventListener deathLinkListener = new DeathLinkEventListener(queue);
        client.getEventManager().registerListener(deathLinkListener);

        // Relays every server text message (PrintJSON) into the player's chat
        // box. The EventManager keeps a strong reference, so no need to store it.
        client.getEventManager().registerListener(new PrintJSONListener(queue));

        // Reports real connection success/failure. Must be its own public
        // listener object — see ConnectionResultListener for why.
        ConnectionResultListener resultListener = new ConnectionResultListener(client, queue, slotName, uuid, lastProcessed);
        client.getEventManager().registerListener(resultListener);
        System.out.println("[ArchipelagoMod] Listeners registered on event manager");

        playerStates.put(uuid, new PlayerAPState(client, lastProcessed, ref, store, slotName, itemListener, deathLinkListener));

        try {
            client.connect(ip + ":" + port);
            System.out.printf("[ArchipelagoMod] connect() returned (WebSocket handshake initiated) — slot=%s%n", slotName);
        } catch (URISyntaxException e) {
            playerStates.remove(uuid);
            throw new RuntimeException("[ArchipelagoMod] Bad URI: " + e.getMessage(), e);
        }
    }

    // tick()
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
        return state != null && isSlotConnected(state);
    }

    /**
     * The AP location IDs that actually exist in this player's connected seed,
     * built from the slot data on connection. Returns {@code null} when the
     * player is not connected or the seed sent no slot data — callers decide
     * whether that means "show nothing" or "show everything".
     *
     * @see ArchipelagoLocationMap#buildActiveLocationIds(HytaleSlotData)
     */
    @javax.annotation.Nullable
    public Set<Long> getActiveLocationIds(@Nonnull PlayerRef playerRef) {
        return activeLocationsByPlayer.get(playerRef.getUuid().toString());
    }

    /**
     * The seed id (seed name + slot) the player is connected to, used to scope
     * save files to one game. {@code null} when the player is not connected, in
     * which case callers must skip persistence (offline progress is not saved).
     */
    @javax.annotation.Nullable
    public String getSeedId(@Nonnull PlayerRef playerRef) {
        return seedIdByPlayer.get(playerRef.getUuid().toString());
    }

    /** As {@link #getSeedId(PlayerRef)} but keyed by the raw uuid string. */
    @javax.annotation.Nullable
    public String getSeedId(@Nonnull String uuid) {
        return seedIdByPlayer.get(uuid);
    }

    /**
     * True when {@code achievementId} maps to a location that exists in this
     * player's connected seed. Returns {@code true} when the player is connected
     * but the seed sent no slot data (legacy "every location is live" fallback),
     * and {@code false} when there is no active table at all.
     */
    public boolean isLocationActive(@Nonnull String uuid, @Nonnull String achievementId) {
        Set<Long> active = activeLocationsByPlayer.get(uuid);
        if (active == null) return seedIdByPlayer.containsKey(uuid); // connected, no slot data -> all live
        Long locId = ArchipelagoLocationMap.getLocationId(achievementId);
        return locId != null && active.contains(locId);
    }

    /** Convenience overload for callers holding a {@link PlayerRef}. */
    public boolean isLocationActive(@Nonnull PlayerRef playerRef, @Nonnull String achievementId) {
        return isLocationActive(playerRef.getUuid().toString(), achievementId);
    }

    /**
     * True only once the AP server has accepted the slot (ConnectionResult.Success)
     * and the socket is still open. The base client's isConnected() goes true as
     * soon as the WebSocket opens — before the slot is confirmed — so it must not
     * be used to gate gameplay (e.g. collecting locations).
     */
    private boolean isSlotConnected(@Nonnull PlayerAPState state) {
        return state.client() instanceof HytaleAPClient client && client.isSlotConnected();
    }

    public void disconnect(@Nonnull PlayerRef playerRef) {
        String uuid = playerRef.getUuid().toString();
        PlayerAPState state = playerStates.remove(uuid);
        if (state != null) closeClient(state.client());
        pendingByPlayer.remove(uuid);
        slotDataByPlayer.remove(uuid);
        activeLocationsByPlayer.remove(uuid);
        seedIdByPlayer.remove(uuid);
    }

    /** Close a client we own without surfacing a spurious "Connection failed" title. */
    private void closeClient(@Nonnull Client client) {
        if (client instanceof HytaleAPClient c) {
            c.closeSilently();
        } else {
            client.close();
        }
    }

    // Dispatchers
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
    // Send a completed location check to the AP server
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
        // Resolve numeric location ID
        Long locationId = ArchipelagoLocationMap.getLocationId(achievementId);
        if (locationId == null) {
            System.out.printf(
                    "[ArchipelagoMod] sendLocationCheck: achievement '%s' has no AP location ID — skipping%n",
                    achievementId);
            return;
        }

        // Check that the player is connected
        String        uuid  = playerRef.getUuid().toString();

        // Only send checks for locations that exist in this seed. If no slot
        // data was received (older seeds), the table is absent and every
        // location is treated as live.
        Set<Long> active = activeLocationsByPlayer.get(uuid);
        if (active != null && !active.contains(locationId)) {
            System.out.printf(
                    "[ArchipelagoMod] sendLocationCheck: '%s' (id=%d) not in this seed's table — skipping%n",
                    achievementId, locationId);
            return;
        }

        PlayerAPState state = playerStates.get(uuid);
        if (state == null || !isSlotConnected(state)) {
            System.out.printf(
                    "[ArchipelagoMod] sendLocationCheck: player %s is not connected — check for '%s' (id=%d) NOT sent%n",
                    uuid, achievementId, locationId);
            return;
        }

        // Send
        System.out.printf(
                "[ArchipelagoMod] Sending location check: '%s' → location id %d%n",
                achievementId, locationId);
        state.client().checkLocation(locationId);

        System.out.printf(
                "[ArchipelagoMod] Location check sent successfully: '%s' (id=%d) for player %s%n",
                achievementId, locationId, uuid);
    }

    // DeathLink

    /**
     * Enable or disable DeathLink for a connected player (the /arch-death_link
     * command). Adds/removes the DeathLink tag on the AP connection.
     *
     * @return true if applied, false if the player is not connected.
     */
    public boolean setDeathLink(@Nonnull PlayerRef playerRef, boolean enabled) {
        PlayerAPState state = playerStates.get(playerRef.getUuid().toString());
        if (state == null || !isSlotConnected(state)) return false;
        state.client().setDeathLinkEnabled(enabled);
        System.out.printf("[ArchipelagoMod] DeathLink %s for %s%n",
                enabled ? "ENABLED" : "DISABLED", state.slotName());
        return true;
    }

    /**
     * Called by the DeathListener whenever a player dies in Hytale. If DeathLink
     * is on and the death wasn't itself caused by an incoming DeathLink, this
     * bounces the death to the AP server.
     */
    public void onLocalDeath(@Nonnull PlayerRef playerRef, @Nonnull String cause) {
        String uuid = playerRef.getUuid().toString();

        // Swallow deaths we caused ourselves from an incoming DeathLink (no loop)
        if (deathLinkSuppress.remove(uuid)) return;

        PlayerAPState state = playerStates.get(uuid);
        if (state == null || !isSlotConnected(state)) return;
        if (!state.client().getTags().contains(DeathLinkHandler.DEATHLINK_TAG)) return;

        System.out.printf("[ArchipelagoMod] Sending DeathLink: source='%s' cause='%s'%n",
                state.slotName(), cause);
        state.client().sendDeathlink(state.slotName(), cause);
    }

    /** Kills the player in Hytale in response to an incoming DeathLink. */
    private void killPlayer(
            @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store
    ) {
        // Mark so the resulting death isn't bounced back to the server
        deathLinkSuppress.add(playerRef.getUuid().toString());
        DeathComponent.tryAddComponent(store, ref,
                new Damage(Damage.NULL_SOURCE, DamageCause.COMMAND, 2.1474836E9f));
        System.out.printf("[ArchipelagoMod] Player %s killed by incoming DeathLink%n",
                playerRef.getUuid());
    }
}