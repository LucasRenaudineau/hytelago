package me.coblaz.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.ItemAchievements;
import me.coblaz.achievements.Registries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class InventoryListener extends EntityEventSystem<EntityStore, InventoryChangeEvent> {

    public InventoryListener() {
        super(InventoryChangeEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InventoryChangeEvent event
    ) {
        // ── Only care about successful ADD transactions ────────────────────────
        if (!(event.getTransaction() instanceof ItemStackTransaction tx)) return;
        if (tx.getAction() != ActionType.ADD)                            return;
        if (!tx.succeeded())                                             return;

        // ── Get the item that was added ───────────────────────────────────────
        ItemStack query = tx.getQuery();
        if (query == null) return;

        List<String> achIds = ItemAchievements.achievementIdsForItem(query.getItemId());
        if (achIds.isEmpty()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        AchievementRegistry reg = Registries.ITEMS;
        for (String achId : achIds) {
            reg.incrementCount(playerRef, achId, query.getQuantity());
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}