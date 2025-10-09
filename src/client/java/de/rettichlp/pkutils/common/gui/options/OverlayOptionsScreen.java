package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.models.config.OverlayOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;

import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.translatable;

public class OverlayOptionsScreen extends OptionsScreen {

    public OverlayOptionsScreen(Screen parent) {
        super(parent, "pkutils.options.overlay.title", false);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.general"), this.textRenderer), Positioner::alignHorizontalCenter);

        // datetime
        addToggleButton(directionalLayoutWidget, "pkutils.options.overlay.datetime", (options, value) -> options.overlay().dateTime(value), options -> options.overlay().dateTime(), 308);

        // payday
        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.payday"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        addToggleButton(directionalLayoutWidget, "pkutils.options.overlay.payday", (options, value) -> options.overlay().payDay(value), options -> options.overlay().payDay(), 308);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget1, "pkutils.options.overlay.payday.salary", (options, value) -> options.overlay().payDaySalary(value), options -> options.overlay().payDaySalary(), 150);
        addToggleButton(directionalLayoutWidget1, "pkutils.options.overlay.payday.experience", (options, value) -> options.overlay().payDayExperience(value), options -> options.overlay().payDayExperience(), 150);

        // car
        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.car"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget2, "pkutils.options.overlay.car.locked", (options, value) -> options.overlay().carLocked(value), options -> options.nameTag().factionInformation(), 150);
        addCyclingButton(directionalLayoutWidget2, "pkutils.options.overlay.car.locked.style.name", OverlayOptions.CarLockedStyle.values(), OverlayOptions.CarLockedStyle::getDisplayName, (options, carLockedStyle) -> options.overlay().carLockedStyle(carLockedStyle), options -> options.overlay().carLockedStyle(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    // disable background rendering to see overlay better
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}
