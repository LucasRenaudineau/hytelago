package me.coblaz.archipelago;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.archipelagomw.Client;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds everything needed to handle future AP events for one player.
 */
// PlayerAPState.java
public record PlayerAPState(
        Client              client,
        AtomicInteger       lastProcessed,
        Ref<EntityStore>    ref,
        Store<EntityStore>  store,
        String              slotName,
        ArchipelagoManager.ItemEventListener      itemListener,       // strong ref prevents GC
        ArchipelagoManager.DeathLinkEventListener deathLinkListener   // strong ref prevents GC
) {}