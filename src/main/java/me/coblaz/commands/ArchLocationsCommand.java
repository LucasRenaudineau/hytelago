package me.coblaz.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.Registries;
import me.coblaz.ui.AchievementListPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.Map;
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
        Registries.LOCATIONS.refreshStatuses(playerRef);
        Player player = store.getComponent(ref, Player.getComponentType());  // ← this was missing
        player.getPageManager().openCustomPage(ref, store,
                new AchievementListPage(playerRef, Registries.LOCATIONS));
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