package de.rettichlp.pkutils.common.gui;

import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;

import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.item.Items.COMPARATOR;
import static net.minecraft.text.Text.translatable;

public class NameTagOptionsScreen extends OptionsScreen {

    public NameTagOptionsScreen() {
        super(new MainOptionsScreen());
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.nametag.general"), this.textRenderer), Positioner::alignHorizontalCenter);

        addButton(directionalLayoutWidget, "pkutils.options.nametag.faction_information", true, 308);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget1, "pkutils.options.nametag.highlight.faction", true, 280);
        addItemButton(directionalLayoutWidget1, COMPARATOR, button -> notificationService.sendWarningNotification("To be implemented"));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget2, "pkutils.options.nametag.highlight.alliance", true, 280);
        addItemButton(directionalLayoutWidget2, COMPARATOR, button -> notificationService.sendWarningNotification("To be implemented"));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.nametag.additional"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget3 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.blacklist", true, 150);
        addButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.contract", true, 150);

        DirectionalLayoutWidget directionalLayoutWidget4 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.houseban", true, 150);
        addButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.wanted", true, 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
