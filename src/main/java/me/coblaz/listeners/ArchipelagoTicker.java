package me.coblaz.listeners;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.archipelago.ArchipelagoManager;

import javax.annotation.Nonnull;

public class ArchipelagoTicker extends EntityTickingSystem<EntityStore> {

    private final Query<EntityStore> query;

    public ArchipelagoTicker() {
        this.query = Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        // Pass the fresh game-thread store; ArchipelagoManager supplies
        // the stable ref captured at connect time.
        ArchipelagoManager.INSTANCE.tick(playerRef, store);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }
}