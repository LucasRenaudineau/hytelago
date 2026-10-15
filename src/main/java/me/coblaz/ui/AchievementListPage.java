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
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.packets.interface_.EventTitleStyle;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.achievements.AchievementStatus;
import me.coblaz.achievements.Category;
import me.coblaz.achievements.PlayerAchievementData;
import me.coblaz.achievements.Registries;
import me.coblaz.archipelago.ArchipelagoManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AchievementListPage extends InteractiveCustomUIPage<AchievementListPage.AchEventData> {

    // Codec

    public static class AchEventData {
        public String action;
        public String value;   // tab category (Tab) or achievement id (CollectOne)

        public static final BuilderCodec<AchEventData> CODEC =
                ((BuilderCodec.Builder<AchEventData>)
                        BuilderCodec.builder(AchEventData.class, AchEventData::new)
                                .append(
                                        new KeyedCodec<>("Action", Codec.STRING),
                                        (AchEventData o, String v) -> o.action = v,
                                        (AchEventData o) -> o.action
                                )
                                .add()
                                .append(
                                        new KeyedCodec<>("Value", Codec.STRING),
                                        (AchEventData o, String v) -> o.value = v,
                                        (AchEventData o) -> o.value
                                )
                                .add())
                        .build();
    }

    // Fields and Constructor

    private final PlayerRef playerRef;
    private final AchievementRegistry registry;
    private final boolean alsoCollectAlreadyCollected;

    // When non-null, only these achievement ids are shown and collectable. Used
    // to restrict the locations table to the rows that exist in the connected
    // seed (built from the slot data). Null means "show every registered row".
    @Nullable private final Set<String> visibleIds;

    // Category tabs are shown for the locations table, per-row Collect buttons
    // for the items table.
    private final boolean showTabs;
    private final boolean showCollectOne;
    @Nonnull private String activeTab = Category.ALL;

    private static final Value<String> TAB_STYLE_ACTIVE   = Value.ref("Pages/AchievementListPage.ui", "ActiveTabStyle");
    private static final Value<String> TAB_STYLE_INACTIVE = Value.ref("Common.ui", "SmallSecondaryTextButtonStyle");

    public AchievementListPage(@Nonnull PlayerRef playerRef,
                               @Nonnull AchievementRegistry registry,
                               boolean alsoCollectAlreadyCollected) {
        this(playerRef, registry, alsoCollectAlreadyCollected, null);
    }

    public AchievementListPage(@Nonnull PlayerRef playerRef,
                               @Nonnull AchievementRegistry registry,
                               boolean alsoCollectAlreadyCollected,
                               @Nullable Set<String> visibleIds) {
        super(playerRef, CustomPageLifetime.CanDismiss, AchEventData.CODEC);
        this.playerRef = playerRef;
        this.registry  = registry;
        this.alsoCollectAlreadyCollected = alsoCollectAlreadyCollected;
        this.visibleIds = visibleIds;
        this.showTabs       = registry == Registries.LOCATIONS;
        this.showCollectOne = registry == Registries.ITEMS;
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
        cmd.set("#TabBar.Visible", showTabs);
        buildPage(cmd, events);
    }

    /** Tab styles, list rows and every event binding (used by build() and refresh()). */
    private void buildPage(
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder   events
    ) {
        if (showTabs) {
            for (int i = 0; i < Category.LOCATION_TABS.size(); i++) {
                String category = Category.LOCATION_TABS.get(i);
                cmd.set("#Tab" + i + ".Style", category.equals(activeTab) ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#Tab" + i,
                        new EventData().append("Action", "Tab").append("Value", category)
                );
            }
        }
        buildAchievementList(cmd, events);
        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CollectButton",
                new EventData().append("Action", "Collect")
        );
    }

    /** Rebuilds the page so rows, colors and tabs update immediately. */
    private void refresh() {
        UICommandBuilder cmd    = new UICommandBuilder();
        UIEventBuilder   events = new UIEventBuilder();
        buildPage(cmd, events);
        sendUpdate(cmd, events, false);
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
        } else if ("CollectOne".equals(data.action) && data.value != null) {
            handleCollectOne(ref, store, data.value);
        } else if ("Tab".equals(data.action) && data.value != null) {
            activeTab = data.value;
            refresh();
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
                    EventTitleStyle.Major
            );
            return;
        }

        List<AchievementDefinition> collected = registry.collectDoneAchievements(
                playerRef, ref, store, alsoCollectAlreadyCollected, visibleIds);

        if (collected.isEmpty()) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("No achievements ready to collect."),
                    Message.raw(""),
                    EventTitleStyle.Major
            );
            return;
        }

        for (AchievementDefinition def : collected) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Achievement collected!"),
                    Message.raw(def.getTitle()),
                    EventTitleStyle.Major
            );
        }

        refresh();
    }

    private void handleCollectOne(
            @Nonnull Ref<EntityStore>   ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String             achievementId
    ) {
        if (!registry.collectOne(playerRef, achievementId, ref, store, alsoCollectAlreadyCollected)) return;

        AchievementDefinition def = registry.findDefinition(achievementId);
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Achievement collected!"),
                Message.raw(def != null ? def.getTitle() : achievementId),
                EventTitleStyle.Major
        );
        refresh();
    }

    // List builder (used by both build() and refresh)

    private void buildAchievementList(
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder   events
    ) {
        List<AchievementDefinition> defs = registry.getDefinitions();

        // Restrict to the seed's locations when a filter is set, and to the
        // active category tab.
        List<AchievementDefinition> filtered = new ArrayList<>();
        for (AchievementDefinition def : defs) {
            if (visibleIds != null && !visibleIds.contains(def.getId())) continue;
            if (showTabs && !Category.ALL.equals(activeTab) && !activeTab.equals(registry.getCategory(playerRef, def))) continue;
            filtered.add(def);
        }
        defs = filtered;

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
            cmd.set(selector + " #AchCount.Text",            data.getCount() + "/" + registry.getNeededCount(playerRef, def));
            cmd.set(selector + " #AchTitle.Style.TextColor", colorFor(data.getStatus()));
            cmd.set(selector + " #AchId.Style.TextColor",    "#6e7da1");
            cmd.set(selector + " #AchCount.Style.TextColor", colorFor(data.getStatus()));

            if (showCollectOne && registry.canCollect(playerRef, def.getId(), alsoCollectAlreadyCollected)) {
                cmd.set(selector + " #CollectOne.Visible", true);
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        selector + " #CollectOne",
                        new EventData().append("Action", "CollectOne").append("Value", def.getId())
                );
            }
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