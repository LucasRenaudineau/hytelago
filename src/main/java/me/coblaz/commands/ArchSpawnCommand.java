package me.coblaz.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class ArchSpawnCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> mobIdArg;

    public ArchSpawnCommand() {
        super("arch-spawn", "Spawn a mob next to you: /arch-spawn <mobId>", false);
        this.mobIdArg = withRequiredArg("mobId", "The NPC role name to spawn", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext      ctx,
            @NonNullDecl Store<EntityStore>  store,
            @NonNullDecl Ref<EntityStore>    ref,
            @NonNullDecl PlayerRef           playerRef,
            @NonNullDecl World               world
    ) {
        String mobId = mobIdArg.get(ctx);

        // Resolve role index
        int roleIndex = NPCPlugin.get().getIndex(mobId);
        if (roleIndex == Integer.MIN_VALUE) {
            reply(playerRef, "Unknown mob ID:", mobId);
            return;
        }

        // Read player position (getPosition returns the live internal
        // Vector3d; we must copy it before offsetting)
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            reply(playerRef, "Could not read your position.", "");
            return;
        }

        Vector3d spawnPos = new Vector3d(transform.getPosition());
        spawnPos.x += 2.0;

        // 3. Spawn
        // null rotation     -> spawnEntity uses NULL_ROTATION internally
        // null spawnModel   -> role's default model is used
        // null pre/postSpawn -> no extra setup needed for a debug spawn
        var result = NPCPlugin.get().spawnEntity(store, roleIndex, spawnPos,
                null, null, null, null);
        if (result == null) {
            reply(playerRef, "Spawn failed.", "Role: " + mobId);
            return;
        }

        reply(playerRef, "Spawned!", mobId);
    }

    private void reply(@Nonnull PlayerRef playerRef,
                       @Nonnull String title,
                       @Nonnull String subtitle) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef, Message.raw(title), Message.raw(subtitle), true
        );
    }
    public static void spawnMob(String mobId, Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        Vector3d pos = new Vector3d(t.getPosition());
        NPCPlugin.get().spawnEntity(store, NPCPlugin.get().getIndex(mobId), pos, null, null, null, null);
    }
}