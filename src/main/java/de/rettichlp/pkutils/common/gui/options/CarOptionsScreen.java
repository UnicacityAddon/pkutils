package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.gui.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;

import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.translatable;

public class CarOptionsScreen extends OptionsScreen {

    public CarOptionsScreen(Screen parent) {
        super(parent, "pkutils.options.car.title");
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.general"), this.textRenderer), Positioner::alignHorizontalCenter);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget1, "pkutils.options.car.fast_find", (options, value) -> options.car().fastFind(value), options -> options.car().fastFind(), 150);
        addToggleButton(directionalLayoutWidget1, "pkutils.options.car.fast_lock", (options, value) -> options.car().fastLock(value), options -> options.car().fastLock(), 150);

        addToggleButton(directionalLayoutWidget, "pkutils.options.car.highlight", (options, value) -> options.car().highlight(value), options -> options.car().highlight(), 308);

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.car.automation.title"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget2, "pkutils.options.car.automation.lock", (options, value) -> options.car().automatedLock(value), options -> options.car().automatedLock(), 150);
        addToggleButton(directionalLayoutWidget2, "pkutils.options.car.automation.start", (options, value) -> options.car().automatedStart(value), options -> options.car().automatedStart(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
