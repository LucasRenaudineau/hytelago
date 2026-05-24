package me.coblaz.listeners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.MobKillAchievements;
import me.coblaz.achievements.Registries;

import javax.annotation.Nonnull;

public class KillListener extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return AllLegacyLivingEntityTypesQuery.INSTANCE;
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

        Damage.Source source = deathInfo.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> sourceRef = entitySource.getRef();
        if (!sourceRef.isValid()) return;

        PlayerRef playerRef = store.getComponent(sourceRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        String entityName = getEntityName(ref, store);

        AchievementRegistry reg = Registries.LOCATIONS;

        // General kill milestones
        reg.incrementCount(playerRef, "first_kill",    1);
        reg.incrementCount(playerRef, "ten_kills",     1);
        reg.incrementCount(playerRef, "thirty_kills",     1);
        reg.incrementCount(playerRef, "fifty_kills",     1);
        reg.incrementCount(playerRef, "seventy_kills",     1);
        reg.incrementCount(playerRef, "hundred_kills", 1);

        // Mob-specific achievement
        String achId = MobKillAchievements.achievementIdForRole(entityName);
        if (achId != null) {
            reg.incrementCount(playerRef, achId, 1);
        }

        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("You killed " + entityName),
                Message.raw(""),
                true
        );
    }

    @Nonnull
    private String getEntityName(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        var npcType = NPCEntity.getComponentType();
        if (npcType == null) return "a creature";

        NPCEntity npc = store.getComponent(ref, npcType);
        if (npc != null) {
            String role = npc.getRoleName();
            if (role != null && !role.isEmpty()) {
                return role;
            }
        }
        return "a creature";
    }
}