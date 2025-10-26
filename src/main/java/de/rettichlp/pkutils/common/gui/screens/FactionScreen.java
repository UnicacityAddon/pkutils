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
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.TextWidget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.screens.FactionScreen.SortingType.NAME;
import static de.rettichlp.pkutils.common.gui.screens.FactionScreen.SortingType.RANK;
import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.NONE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.of;

public class FactionScreen extends OptionsScreen {

    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;

    private final Faction faction;
    private final SortingType sortingType;
    private final TableHeaderTextWidget.SortingDirection sortingDirection;

    private int offset;

    public FactionScreen(Faction faction,
                         SortingType sortingType,
                         TableHeaderTextWidget.SortingDirection sortingDirection,
                         int offset) {
        super(new GameMenuScreen(true), of("Faction Members"));
        this.faction = faction;
        this.sortingType = sortingType;
        this.sortingDirection = sortingDirection;
        this.offset = offset;
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));
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

        this.client.setScreen(new FactionScreen(this.faction, this.sortingType, this.sortingDirection, this.offset));

        return mouseScroll;
    }

    private Set<FactionMember> getSortedFactionMembers() {
        Set<FactionMember> factionMembers = storage.getFactionEntries().stream()
                .filter(factionEntry -> factionEntry.faction() == this.faction)
                .findFirst()
                .map(FactionEntry::members)
                .orElse(emptySet());

        return this.sortingType.apply(factionMembers, this.sortingDirection);
    }

    private @NotNull DirectionalLayoutWidget getHeaderDirectionalLayoutWidget() {
        List<ActivityEntry.Type> activityTypes = stream(ActivityEntry.Type.values())
                .filter(type -> type.isAllowedForFaction(this.faction))
                .toList();

        DirectionalLayoutWidget directionalLayoutWidget = vertical().spacing(4);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));

        TableHeaderTextWidget nameTableHeaderTextWidget = new TableHeaderTextWidget(of("Name"), sortingDirection -> this.client.setScreen(new FactionScreen(this.faction, NAME, sortingDirection, this.offset)), this.sortingType == NAME ? this.sortingDirection : NONE);
        nameTableHeaderTextWidget.setWidth(80);
        directionalLayoutWidget1.add(nameTableHeaderTextWidget);

        TableHeaderTextWidget rangTableHeaderTextWidget = new TableHeaderTextWidget(of("Rang"), sortingDirection -> this.client.setScreen(new FactionScreen(this.faction, RANK, sortingDirection, this.offset)), this.sortingType == RANK ? this.sortingDirection : NONE);
        rangTableHeaderTextWidget.setWidth(80);
        directionalLayoutWidget1.add(rangTableHeaderTextWidget);

        TextWidget activityTextWidget = new TextWidget(of("Aktivitäten"), TEXT_RENDERER);
        activityTextWidget.setWidth(activityTypes.size() * 80 + (activityTypes.size() - 1) * 8);
        directionalLayoutWidget1.add(activityTextWidget);

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8), positioner -> positioner.marginBottom(16));

        directionalLayoutWidget2.add(new EmptyWidget(80, 0));
        directionalLayoutWidget2.add(new EmptyWidget(80, 0));

        activityTypes.forEach(type -> {
            TextWidget activityEntryTextWidget = new TextWidget(of(type.getDisplayName()), TEXT_RENDERER);
            activityEntryTextWidget.setWidth(80);
            directionalLayoutWidget2.add(activityEntryTextWidget);
        });

        return directionalLayoutWidget;
    }

    private DirectionalLayoutWidget getMemberDirectionalLayoutWidget() {
        DirectionalLayoutWidget directionalLayoutWidget = vertical().spacing(4);

        getSortedFactionMembers().stream().skip(this.offset).limit(getPageLimit()).forEach(factionMember -> {
            DirectionalLayoutWidget memberDirectionalLayoutWidget = directionalLayoutWidget.add(horizontal().spacing(8), positioner -> positioner.marginTop(4));

            TextWidget nameTextWidget = new TextWidget(of(factionMember.playerName()), TEXT_RENDERER);
            nameTextWidget.setWidth(80);
            memberDirectionalLayoutWidget.add(nameTextWidget);

            TextWidget rangTextWidget = new TextWidget(of(String.valueOf(factionMember.rank())), TEXT_RENDERER);
            rangTextWidget.setWidth(80);
            memberDirectionalLayoutWidget.add(rangTextWidget);
        });

        return directionalLayoutWidget;
    }

    private int getPageLimit() {
        int contentHeight = this.layout.getContentHeight();
        return contentHeight / 20;
    }

    public enum SortingType {

        NAME,
        RANK;

        @Contract("_, _ -> new")
        public @NotNull Set<FactionMember> apply(Collection<FactionMember> factionMembers,
                                                 TableHeaderTextWidget.SortingDirection sortingDirection) {
            Comparator<FactionMember> factionMemberComparator = switch (this) {
                case NAME -> comparing(FactionMember::playerName);
                case RANK -> comparingInt(FactionMember::rank);
            };

            factionMemberComparator = sortingDirection.apply(factionMemberComparator);

            return factionMembers.stream().sorted(factionMemberComparator).collect(toCollection(LinkedHashSet::new));
        }
    }
}
