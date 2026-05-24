package me.coblaz.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ArchHelpCommand extends AbstractPlayerCommand {

    public ArchHelpCommand() {
        super("arch-help", "Shows available Archipelago commands", false);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext         ctx,
            @NonNullDecl Store<EntityStore>     store,
            @NonNullDecl Ref<EntityStore>       ref,
            @NonNullDecl PlayerRef              playerRef,
            @NonNullDecl World                  world
    ) {
        ctx.sendMessage(Message.raw("Here is the list of in-game commands :"));
        ctx.sendMessage(Message.raw("/arch-help : lists the commands of Hytelago mod"));
        ctx.sendMessage(Message.raw("/arch-connect <ip address> <port> <player_name> : connects yourself to the archipelago server"));
        ctx.sendMessage(Message.raw("/arch-items : shows the items table"));
        ctx.sendMessage(Message.raw("/arch-locations : shows the locations table"));
        ctx.sendMessage(Message.raw("/arch-collect <achievementId> : force-collect an achievement (either from the item table or from the location table)"));
        ctx.sendMessage(Message.raw("/arch-set_count <count> <achievementId> : sets the count of an achievement to count"));
        ctx.sendMessage(Message.raw("/arch-set_state <achievementId> <NOT_DONE|DONE|COLLECTED> : changes the state of an achievement"));
        ctx.sendMessage(Message.raw("/arch-spawn <mobId> : spawns a mob on the player"));
        ctx.sendMessage(Message.raw("For more help, please check the github repository of the Hytelago mod : https://github.com/LucasRenaudineau/hytelago"));
    }
}