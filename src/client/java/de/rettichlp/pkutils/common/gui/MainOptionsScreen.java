package de.rettichlp.pkutils.common.gui;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.item.Items.COMPARATOR;

public class MainOptionsScreen extends OptionsScreen {

    public MainOptionsScreen() {
        super(new GameMenuScreen(true));
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(8));

        directionalLayoutWidget.add(ButtonWidget.builder(Text.of("test"), button -> this.client.setScreen(new NameTagOptionsScreen())).build());

        //        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(DirectionalLayoutWidget.horizontal()).spacing(8);
//        directionalLayoutWidget2.add(this.settings.getFov().createWidget(this.client.options));
//        directionalLayoutWidget2.add(this.createTopRightButton());

        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(new TextWidget(Text.of("Nametag"), this.textRenderer), 2, gridWidget.copyPositioner().marginTop(10).alignHorizontalCenter());
        adder.add(new ToggleButtonWidget(10, 10, 30, 30, true));

        adder.add(ButtonWidget.builder(Text.of("test1"), (button) -> {
            System.out.println("test1");
        }).width(204).build(), 2, gridWidget.copyPositioner().marginTop(50));

        adder.add(ButtonWidget.builder(Text.of("test1"), (button) -> {
            System.out.println("test1");
        }).width(204).build(), 2, gridWidget.copyPositioner().marginTop(50));
        adder.add(ButtonWidget.builder(Text.translatable("options.mouse_settings"), (button) -> System.out.println("test1")).width(98).build());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5F, 0F);
        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    private Widget getNameTagSection() {
        DirectionalLayoutWidget directionalLayoutWidget = vertical().spacing(4);
        directionalLayoutWidget.add(new TextWidget(Text.of("Nametag"), this.textRenderer), Positioner::alignHorizontalCenter);

        addButton(directionalLayoutWidget, "pkutils.options.nametag.faction.information", true, 308);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget1, "pkutils.nametag.highlight.faction", true, 280);
        addItemButton(directionalLayoutWidget1, COMPARATOR, button -> System.out.println("Item Button clicked"));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget2, "pkutils.nametag.highlight.alliance", true, 280);
        addItemButton(directionalLayoutWidget2, COMPARATOR, button -> System.out.println("Item Button clicked"));

        addButton(directionalLayoutWidget, "pkutils.nametag.faction.additional", true, 308);

        return directionalLayoutWidget;
    }
}
