package de.rettichlp.pkutils.common.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;

import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.item.Items.COMPARATOR;
import static net.minecraft.text.Text.translatable;

public class NameTagOptionsScreen extends OptionsScreen {

    public NameTagOptionsScreen(Screen parent) {
        super(parent, "pkutils.options.nametag.title");
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.sections.general"), this.textRenderer), Positioner::alignHorizontalCenter);

        addToggleButton(directionalLayoutWidget, "pkutils.options.nametag.faction_information", (options, value) -> options.nameTag().factionInformation(value), options -> options.nameTag().factionInformation(), 308);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget1, "pkutils.options.nametag.highlight.faction", (options, value) -> options.nameTag().highlightFaction(value), options -> options.nameTag().highlightFaction(), 280);
        addItemButton(directionalLayoutWidget1, COMPARATOR, button -> notificationService.sendWarningNotification("To be implemented"));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget2, "pkutils.options.nametag.highlight.alliance", (options, value) -> options.nameTag().highlightAlliance(value), options -> options.nameTag().highlightAlliance(), 280);
        addItemButton(directionalLayoutWidget2, COMPARATOR, button -> notificationService.sendWarningNotification("To be implemented"));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.nametag.additional"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget3 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.blacklist", (options, value) -> options.nameTag().additionalBlacklist(value), options -> options.nameTag().additionalBlacklist(), 150);
        addToggleButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.contract", (options, value) -> options.nameTag().additionalContract(value), options -> options.nameTag().additionalContract(), 150);

        DirectionalLayoutWidget directionalLayoutWidget4 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.houseban", (options, value) -> options.nameTag().additionalHouseban(value), options -> options.nameTag().additionalHouseban(), 150);
        addToggleButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.wanted", (options, value) -> options.nameTag().additionalWanted(value), options -> options.nameTag().additionalWanted(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
