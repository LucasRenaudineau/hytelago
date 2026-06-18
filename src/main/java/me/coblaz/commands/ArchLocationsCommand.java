package me.coblaz.commands;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.MemoriesAchievements;
import me.coblaz.achievements.Registries;
import me.coblaz.achievements.RegionsAchievements;
import me.coblaz.archipelago.ArchipelagoManager;
import me.coblaz.ui.AchievementListPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.coblaz.achievements.LocationsPropertyAchievements;

public class ArchLocationsCommand extends AbstractPlayerCommand {

    public ArchLocationsCommand() {
        super("arch-locations", "Opens your achievements (locations) list", false);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext     ctx,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore>   ref,
            @NonNullDecl PlayerRef          playerRef,
            @NonNullDecl World              world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        // The locations table only exists once the AP server has confirmed the
        // slot and told us which locations this seed contains. Before that there
        // is no seed to mirror, so show nothing rather than the full catalogue.
        if (!ArchipelagoManager.INSTANCE.isConnected(playerRef)) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Not connected to Archipelago."),
                    Message.raw("Run /arch-connect first to load your locations."),
                    true);
            return;
        }

        // Only the locations that exist in the connected seed (built from the
        // slot data) are shown and collectable.
        Set<String> visibleIds = visibleLocationIds(playerRef);

        syncItemCounts(Registries.LOCATIONS, playerRef, ref, store);  // was defined but never called — bug fix
        syncMemoriesCount(Registries.LOCATIONS, playerRef);                  // new
        syncCurrentRegion(Registries.LOCATIONS, playerRef, player);
        Registries.LOCATIONS.refreshStatuses(playerRef);
        player.getPageManager().openCustomPage(ref, store,
                new AchievementListPage(playerRef, Registries.LOCATIONS, false, visibleIds));
    }

    /**
     * The registered achievement ids whose AP location is part of this player's
     * seed. Returns {@code null} (no filter — show all) only for connected seeds
     * that sent no slot data, preserving the legacy "every location is live"
     * fallback used elsewhere in the mod.
     */
    private Set<String> visibleLocationIds(PlayerRef playerRef) {
        return Registries.LOCATIONS.getDefinitions().stream()
                .map(AchievementDefinition::getId)
                .filter(id -> ArchipelagoManager.INSTANCE.isLocationActive(playerRef, id))
                .collect(Collectors.toSet());
    }

    private void syncCurrentRegion(AchievementRegistry reg, PlayerRef playerRef, Player player) {
        WorldMapTracker.ZoneDiscoveryInfo zone = player.getWorldMapTracker().getCurrentZone();
        if (zone == null) return;
        RegionsAchievements.incrementCurrentRegion(reg, playerRef, zone.regionName());
    }

    private void syncMemoriesCount(AchievementRegistry reg, PlayerRef playerRef) {
        // getMemoriesLevel() returns a stepped *level* (1, 2, 3…) derived from the
        // MemoriesAmountPerLevel thresholds, not the raw memory count. The milestone
        // achievements (memories_1…150) expect the actual count, so use the recorded
        // memory set size directly.
        int count = MemoriesPlugin.get().getRecordedMemories().size();
        MemoriesAchievements.updateMemoriesCount(reg, playerRef, count);
    }

    private void syncItemCounts(
            AchievementRegistry     reg,
            PlayerRef               playerRef,
            Ref<EntityStore>        ref,
            Store<EntityStore>      store
    ) {
        InventoryComponent.Hotbar   hotbar   = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage  storage  = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());

        // Group entries by itemId to avoid scanning inventory multiple times per item
        Map<String, List<LocationsPropertyAchievements.Entry>> byItem = LocationsPropertyAchievements.ALL.stream()
                .collect(Collectors.groupingBy(e -> e.itemId().toLowerCase()));

        for (Map.Entry<String, List<LocationsPropertyAchievements.Entry>> group : byItem.entrySet()) {
            Predicate<ItemStack> match = item ->
                    group.getKey().equalsIgnoreCase(item.getItemId());

            int count = 0;
            if (hotbar   != null) count += hotbar.getInventory().countItemStacks(match);
            if (storage  != null) count += storage.getInventory().countItemStacks(match);
            if (backpack != null) count += backpack.getInventory().countItemStacks(match);

            for (LocationsPropertyAchievements.Entry entry : group.getValue()) {
                reg.setCount(playerRef, entry.achievementId(), count);
            }
        }
    }
}