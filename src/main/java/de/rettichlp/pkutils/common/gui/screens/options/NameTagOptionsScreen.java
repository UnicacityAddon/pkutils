package de.rettichlp.pkutils.common.gui.screens.options;

import de.rettichlp.pkutils.common.gui.screens.OptionsScreen;
import de.rettichlp.pkutils.common.models.Color;
import de.rettichlp.pkutils.common.models.Faction;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.models.Color.WHITE;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.util.Arrays.stream;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public class NameTagOptionsScreen extends OptionsScreen {

    private static final Text TEXT_NAMETAG = translatable("pkutils.options.text.nametag");
    private static final Text TEXT_FACTION = translatable("pkutils.options.text.faction");
    private static final Text TEXT_COLOR = translatable("pkutils.options.text.color");
    private static final Text TEXT_ADDITIONAL = translatable("pkutils.options.text.additional");
    private static final Text NAMETAG_FACTION_INFORMATION_NAME = translatable("pkutils.options.nametag.faction.information.name");
    private static final Text NAMETAG_FACTION_INFORMATION_TOOLTIP = translatable("pkutils.options.nametag.faction.information.tooltip");
    private static final Text NAMETAG_ADDITIONAL_BLACKLIST_NAME = translatable("pkutils.options.nametag.additional.blacklist.name");
    private static final Text NAMETAG_ADDITIONAL_BLACKLIST_TOOLTIP = translatable("pkutils.options.nametag.additional.blacklist.tooltip");
    private static final Text NAMETAG_ADDITIONAL_CONTRACT_NAME = translatable("pkutils.options.nametag.additional.contract.name");
    private static final Text NAMETAG_ADDITIONAL_CONTRACT_TOOLTIP = translatable("pkutils.options.nametag.additional.contract.tooltip");
    private static final Text NAMETAG_ADDITIONAL_HOUSEBAN_NAME = translatable("pkutils.options.nametag.additional.houseban.name");
    private static final Text NAMETAG_ADDITIONAL_HOUSEBAN_TOOLTIP = translatable("pkutils.options.nametag.additional.houseban.tooltip");
    private static final Text NAMETAG_ADDITIONAL_WANTED_NAME = translatable("pkutils.options.nametag.additional.wanted.name");
    private static final Text NAMETAG_ADDITIONAL_WANTED_TOOLTIP = translatable("pkutils.options.nametag.additional.wanted.tooltip");

    public NameTagOptionsScreen(Screen parent) {
        super(parent, TEXT_NAMETAG);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(TEXT_FACTION, this.textRenderer), Positioner::alignHorizontalCenter);

        renderService.addToggleButton(directionalLayoutWidget, NAMETAG_FACTION_INFORMATION_NAME, NAMETAG_FACTION_INFORMATION_TOOLTIP, (options, value) -> options.nameTag().factionInformation(value), options -> options.nameTag().factionInformation(), 308);

        directionalLayoutWidget.add(new TextWidget(TEXT_COLOR, this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));
        directionalLayoutWidget.add(getFactionColorOptions());

        directionalLayoutWidget.add(new TextWidget(TEXT_ADDITIONAL, this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget3 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addToggleButton(directionalLayoutWidget3, NAMETAG_ADDITIONAL_BLACKLIST_NAME, NAMETAG_ADDITIONAL_BLACKLIST_TOOLTIP, (options, value) -> options.nameTag().additionalBlacklist(value), options -> options.nameTag().additionalBlacklist(), 150);
        renderService.addToggleButton(directionalLayoutWidget3, NAMETAG_ADDITIONAL_CONTRACT_NAME, NAMETAG_ADDITIONAL_CONTRACT_TOOLTIP, (options, value) -> options.nameTag().additionalContract(value), options -> options.nameTag().additionalContract(), 150);

        DirectionalLayoutWidget directionalLayoutWidget4 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addToggleButton(directionalLayoutWidget4, NAMETAG_ADDITIONAL_HOUSEBAN_NAME, NAMETAG_ADDITIONAL_HOUSEBAN_TOOLTIP, (options, value) -> options.nameTag().additionalHouseban(value), options -> options.nameTag().additionalHouseban(), 150);
        renderService.addToggleButton(directionalLayoutWidget4, NAMETAG_ADDITIONAL_WANTED_NAME, NAMETAG_ADDITIONAL_WANTED_TOOLTIP, (options, value) -> options.nameTag().additionalWanted(value), options -> options.nameTag().additionalWanted(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    private @NotNull GridWidget getFactionColorOptions() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        stream(Faction.values())
                .filter(faction -> faction != NULL)
                .map(faction -> new CyclingButtonWidget.Builder<>(Color::getDisplayName)
                        .values(Color.values())
                        .initially(configuration.getOptions().nameTag().highlightFactions().getOrDefault(faction, WHITE))
                        .build(of(faction.getDisplayName()), (button, value) -> configuration.getOptions().nameTag().highlightFactions().put(faction, value)))
                .forEach(adder::add);

        return gridWidget;
    }
}
