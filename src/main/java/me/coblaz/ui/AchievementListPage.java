package me.coblaz.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.AchievementStatus;
import me.coblaz.achievements.PlayerAchievementData;
import me.coblaz.achievements.Registries;
import me.coblaz.archipelago.ArchipelagoManager;

import javax.annotation.Nonnull;
import java.util.List;

public class AchievementListPage extends InteractiveCustomUIPage<AchievementListPage.AchEventData> {

    // Codec

    public static class AchEventData {
        public String action;

        public static final BuilderCodec<AchEventData> CODEC =
                ((BuilderCodec.Builder<AchEventData>)
                        BuilderCodec.builder(AchEventData.class, AchEventData::new)
                                .append(
                                        new KeyedCodec<>("Action", Codec.STRING),
                                        (AchEventData o, String v) -> o.action = v,
                                        (AchEventData o) -> o.action
                                )
                                .add())
                        .build();
    }

    // Fields and Constructor

    private final PlayerRef playerRef;
    private final AchievementRegistry registry;
    private final boolean alsoCollectAlreadyCollected;

    public AchievementListPage(@Nonnull PlayerRef playerRef,
                               @Nonnull AchievementRegistry registry,
                               boolean alsoCollectAlreadyCollected) {
        super(playerRef, CustomPageLifetime.CanDismiss, AchEventData.CODEC);
        this.playerRef = playerRef;
        this.registry  = registry;
        this.alsoCollectAlreadyCollected = alsoCollectAlreadyCollected;
    }

    // Build

    @Override
    public void build(
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull UICommandBuilder   cmd,
            @Nonnull UIEventBuilder     events,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/AchievementListPage.ui");
        buildAchievementList(cmd, events);
        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CollectButton",
                new EventData().append("Action", "Collect")
        );
    }

    // Event handling

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore>       ref,
            @Nonnull Store<EntityStore>     store,
            @Nonnull AchEventData           data
    ) {
        if ("Collect".equals(data.action)) {
            handleCollect(ref, store);
        }
    }

    // Collect logic

    private void handleCollect(
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store
    ) {
        // Locations can only be collected while connected to the Archipelago
        // server; otherwise the click has no effect on the table.
        if (registry == Registries.LOCATIONS
                && !ArchipelagoManager.INSTANCE.isConnected(playerRef)) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Not connected to Archipelago."),
                    Message.raw("Connect before collecting locations."),
                    true
            );
            return;
        }

        List<AchievementDefinition> collected = registry.collectDoneAchievements(
                playerRef, ref, store, alsoCollectAlreadyCollected);

        if (collected.isEmpty()) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("No achievements ready to collect."),
                    Message.raw(""),
                    true
            );
            return;
        }

        for (AchievementDefinition def : collected) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Achievement collected!"),
                    Message.raw(def.getTitle()),
                    true
            );
        }

        // Refresh the list so rows update color immediately
        UICommandBuilder refresh = new UICommandBuilder();
        UIEventBuilder   events  = new UIEventBuilder();
        buildAchievementList(refresh, events);
        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CollectButton",
                new EventData().append("Action", "Collect")
        );
        sendUpdate(refresh, events, false);
    }

    // List builder (used by both build() and refresh)

    private void buildAchievementList(
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder   events
    ) {
        List<AchievementDefinition> defs = registry.getDefinitions();

        cmd.clear("#AchievementList");

        if (defs.isEmpty()) {
            cmd.appendInline("#AchievementList",
                    "Label { Text: \"No achievements defined.\"; Anchor: (Height: 40); " +
                            "Style: (FontSize: 14, TextColor: #6e7da1, " +
                            "HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        for (int i = 0; i < defs.size(); i++) {
            AchievementDefinition def      = defs.get(i);
            PlayerAchievementData data     = registry.getData(playerRef, def.getId());
            String                selector = "#AchievementList[" + i + "]";

            cmd.append("#AchievementList", "Pages/AchievementEntry.ui");
            cmd.set(selector + " #AchTitle.Text",            def.getTitle());
            cmd.set(selector + " #AchId.Text",               def.getId());
            cmd.set(selector + " #AchCount.Text",            data.getCount() + "/" + def.getNeededCount());
            cmd.set(selector + " #AchTitle.Style.TextColor", colorFor(data.getStatus()));
            cmd.set(selector + " #AchId.Style.TextColor",    "#6e7da1");
            cmd.set(selector + " #AchCount.Style.TextColor", colorFor(data.getStatus()));
        }
    }

    @Nonnull
    private String colorFor(@Nonnull AchievementStatus status) {
        return switch (status) {
            case NOT_DONE  -> "#ff4444";
            case DONE      -> "#ffdd44";
            case COLLECTED -> "#44ff88";
        };
    }
}