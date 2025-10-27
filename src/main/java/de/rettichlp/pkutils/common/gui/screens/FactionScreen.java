package de.rettichlp.pkutils.common.gui.screens;

import de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget;
import de.rettichlp.pkutils.common.models.ActivityEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionEntry;
import de.rettichlp.pkutils.common.models.FactionMember;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.screens.FactionScreen.SortingType.NAME;
import static de.rettichlp.pkutils.common.gui.screens.FactionScreen.SortingType.RANK;
import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.NONE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.WHITE;

public class FactionScreen extends OptionsScreen {

    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;

    private final Faction faction;
    private final SortingType sortingType;
    private final TableHeaderTextWidget.SortingDirection sortingDirection;
    private final Map<String, Map<String, Integer>> activities;
    private final LocalDateTime from;
    private final LocalDateTime to;

    private int offset;

    public FactionScreen(Faction faction,
                         SortingType sortingType,
                         TableHeaderTextWidget.SortingDirection sortingDirection,
                         Map<String, Map<String, Integer>> activities,
                         LocalDateTime from,
                         LocalDateTime to,
                         int offset) {
        super(new GameMenuScreen(true), of("Faction Members"));
        this.faction = faction;
        this.sortingType = sortingType;
        this.sortingDirection = sortingDirection;
        this.activities = activities;
        this.from = from;
        this.to = to;
        this.offset = offset;
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));

        renderService.addButton(directionalLayoutWidget1, of("←⇽"), button -> {
            LocalDateTime newFrom = this.from.minusWeeks(1);
            LocalDateTime newTo = this.to.minusWeeks(1);

            api.getActivityPlayers(newFrom, newTo, this.faction.getMembers().stream().map(FactionMember::playerName).toList(), activities -> this.client.execute(() -> {
                FactionScreen factionScreen = new FactionScreen(this.faction, this.sortingType, this.sortingDirection, activities, newFrom, newTo, 0);
                this.client.setScreen(factionScreen);
            }));
        }, 30);

        renderService.addButton(directionalLayoutWidget1, empty(), button -> {}, 300);

        renderService.addButton(directionalLayoutWidget1, of("→⇾"), button -> {
            LocalDateTime newFrom = this.from.plusWeeks(1);
            LocalDateTime newTo = this.to.plusWeeks(1);

            api.getActivityPlayers(newFrom, newTo, this.faction.getMembers().stream().map(FactionMember::playerName).toList(), activities -> this.client.execute(() -> {
                FactionScreen factionScreen = new FactionScreen(this.faction, this.sortingType, this.sortingDirection, activities, newFrom, newTo, 0);
                this.client.setScreen(factionScreen);
            }));
        }, 30);

        directionalLayoutWidget.add(getHeaderDirectionalLayoutWidget(), positioner -> positioner.marginBottom(4));
        directionalLayoutWidget.add(getMemberDirectionalLayoutWidget());
        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        boolean mouseScroll = super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int operant = 0;
        if (verticalAmount < 0) {
            operant = 1;
        } else if (verticalAmount > 0) {
            operant = -1;
        }

        this.offset = max(0, min(getSortedFactionMembers().size() - getPageLimit(), this.offset + operant));

        this.client.setScreen(new FactionScreen(this.faction, this.sortingType, this.sortingDirection, this.activities, this.from, this.to, this.offset));

        return mouseScroll;
    }

    private Set<FactionMember> getSortedFactionMembers() {
        Set<FactionMember> factionMembers = storage.getFactionEntries().stream()
                .filter(factionEntry -> factionEntry.faction() == this.faction)
                .findFirst()
                .map(FactionEntry::members)
                .orElse(emptySet());

        return this.sortingType.apply(factionMembers, this.activities, this.sortingDirection);
    }

    private @NotNull DirectionalLayoutWidget getHeaderDirectionalLayoutWidget() {
        List<ActivityEntry.Type> activityTypes = getActivityTypes();

        DirectionalLayoutWidget directionalLayoutWidget = vertical().spacing(4);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));

        TextWidget userTextWidget = new TextWidget(of("User"), TEXT_RENDERER);
        userTextWidget.setWidth(168);
        directionalLayoutWidget1.add(userTextWidget);

        TextWidget activityTextWidget = new TextWidget(of("Aktivitäten"), TEXT_RENDERER);
        activityTextWidget.setWidth(activityTypes.size() * 80 + (activityTypes.size() - 1) * 8);
        directionalLayoutWidget1.add(activityTextWidget);

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8), positioner -> positioner.marginBottom(16));

        TableHeaderTextWidget nameTableHeaderTextWidget = new TableHeaderTextWidget(of("Name"), sortingDirection -> this.client.setScreen(new FactionScreen(this.faction, NAME, sortingDirection, this.activities, this.from, this.to, this.offset)), this.sortingType == NAME ? this.sortingDirection : NONE);
        nameTableHeaderTextWidget.setWidth(80);
        directionalLayoutWidget2.add(nameTableHeaderTextWidget);

        TableHeaderTextWidget rangTableHeaderTextWidget = new TableHeaderTextWidget(of("Rang"), sortingDirection -> this.client.setScreen(new FactionScreen(this.faction, RANK, sortingDirection, this.activities, this.from, this.to, this.offset)), this.sortingType == RANK ? this.sortingDirection : NONE);
        rangTableHeaderTextWidget.setWidth(80);
        directionalLayoutWidget2.add(rangTableHeaderTextWidget);

        activityTypes.forEach(type -> {
            SortingType sortingType = SortingType.valueOf(type.name());
            TableHeaderTextWidget activityTableHeaderTextWidget = new TableHeaderTextWidget(of(type.getDisplayName()), sortingDirection -> this.client.setScreen(new FactionScreen(this.faction, sortingType, sortingDirection, this.activities, this.from, this.to, this.offset)), this.sortingType == sortingType ? this.sortingDirection : NONE);
            activityTableHeaderTextWidget.setWidth(80);
            directionalLayoutWidget2.add(activityTableHeaderTextWidget, Positioner::alignHorizontalCenter);
        });

        return directionalLayoutWidget;
    }

    private DirectionalLayoutWidget getMemberDirectionalLayoutWidget() {
        DirectionalLayoutWidget directionalLayoutWidget = vertical().spacing(4);

        getSortedFactionMembers().stream().skip(this.offset).limit(getPageLimit()).forEach(factionMember -> {
            Formatting color = player.getGameProfile().getName().equals(factionMember.playerName()) ? GREEN : WHITE;

            Map<String, Integer> playerActivities = this.activities.getOrDefault(factionMember.playerName(), new HashMap<>());

            DirectionalLayoutWidget memberDirectionalLayoutWidget = directionalLayoutWidget.add(horizontal().spacing(8), positioner -> positioner.marginTop(4));

            TextWidget nameTextWidget = new TextWidget(of(factionMember.playerName()).copy().formatted(color), TEXT_RENDERER);
            nameTextWidget.setWidth(80);
            memberDirectionalLayoutWidget.add(nameTextWidget);

            TextWidget rangTextWidget = new TextWidget(of(valueOf(factionMember.rank())).copy().formatted(color), TEXT_RENDERER);
            rangTextWidget.setWidth(80);
            memberDirectionalLayoutWidget.add(rangTextWidget);

            getActivityTypes().forEach(type -> {
                int activityAmount = playerActivities.getOrDefault(type.name(), 0);
                TextWidget activityEntryTextWidget = new TextWidget(of(valueOf(activityAmount)).copy().formatted(color), TEXT_RENDERER);
                activityEntryTextWidget.setWidth(80);
                memberDirectionalLayoutWidget.add(activityEntryTextWidget);
            });
        });

        return directionalLayoutWidget;
    }

    private int getPageLimit() {
        int contentHeight = this.layout.getContentHeight();
        return contentHeight / 20;
    }

    private @NotNull @Unmodifiable List<ActivityEntry.Type> getActivityTypes() {
        return stream(ActivityEntry.Type.values())
                .filter(type -> type.isAllowedForFaction(this.faction))
                .toList();
    }

    public enum SortingType {

        NAME,
        RANK,
        ARREST,
        ARREST_KILL,
        EMERGENCY_SERVICE,
        MAJOR_EVENT,
        PARK_TICKET,
        REINFORCEMENT,
        REVIVE;

        @Contract("_, _, _ -> new")
        public @NotNull Set<FactionMember> apply(Collection<FactionMember> factionMembers,
                                                 Map<String, Map<String, Integer>> activities,
                                                 TableHeaderTextWidget.SortingDirection sortingDirection) {
            Comparator<FactionMember> factionMemberComparator = switch (this) {
                case NAME -> comparing(FactionMember::playerName);
                case RANK -> comparingInt(FactionMember::rank);
                default ->
                        comparingInt(factionMember -> activities.getOrDefault(factionMember.playerName(), new HashMap<>()).getOrDefault(this.name(), 0));
            };

            factionMemberComparator = sortingDirection.apply(factionMemberComparator);

            return factionMembers.stream().sorted(factionMemberComparator).collect(toCollection(LinkedHashSet::new));
        }
    }
}
