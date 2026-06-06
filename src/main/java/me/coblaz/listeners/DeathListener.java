package me.coblaz.listeners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.DeathAchievements;
import me.coblaz.achievements.Registries;
import me.coblaz.archipelago.ArchipelagoManager;

import javax.annotation.Nonnull;

public class DeathListener extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Only fire for player entities, same as PlayerDeathMarker
        return Player.getComponentType();
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore>           ref,
            @Nonnull DeathComponent             component,
            @Nonnull Store<EntityStore>         store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Damage deathInfo = component.getDeathInfo();
        if (deathInfo == null) return;

        // Get the PlayerRef of the dying player (not the killer; opposite of KillListener)
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        AchievementRegistry reg = Registries.LOCATIONS;

        // Resolve cause ID

        String causeId = resolveCauseId(deathInfo);

        // DeathLink: bounce this death to the AP server (no-op if DeathLink is off)
        ArchipelagoManager.INSTANCE.onLocalDeath(playerRef, causeId != null ? causeId : "unknown");

        if (causeId == null) return;

        // Increment matching achievement
        String achId = DeathAchievements.achievementIdForCause(causeId);
        if (achId != null) {
            reg.incrementCount(playerRef, achId, 1);
        }
    }

    private String resolveCauseId(@Nonnull Damage deathInfo) {
        // EnvironmentSource carries its own type string (lava, fire,...)
        // Check this first since it's more specific than the DamageCause asset
        if (deathInfo.getSource() instanceof Damage.EnvironmentSource env) {
            return env.getType().toLowerCase();
        }

        // For all other sources, use the DamageCause asset ID (fall, drowning,...)
        DamageCause cause = DamageCause.getAssetMap().getAsset(deathInfo.getDamageCauseIndex());
        if (cause != null) {
            return cause.getId().toLowerCase();
        }

        return null;
    }
}