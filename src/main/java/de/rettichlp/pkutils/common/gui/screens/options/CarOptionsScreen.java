package de.rettichlp.pkutils.common.gui.screens.options;

import de.rettichlp.pkutils.common.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.translatable;

public class CarOptionsScreen extends OptionsScreen {

    private static final Text TEXT_CAR = translatable("pkutils.options.text.car");
    private static final Text TEXT_GENERAL = translatable("pkutils.options.text.general");
    private static final Text TEXT_AUTOMATION = translatable("pkutils.options.text.automation");
    private static final Text CAR_GENERAL_FAST_FIND_NAME = translatable("pkutils.options.car.general.fast_find.name");
    private static final Text CAR_GENERAL_FAST_FIND_TOOLTIP = translatable("pkutils.options.car.general.fast_find.tooltip");
    private static final Text CAR_GENERAL_FAST_LOCK_NAME = translatable("pkutils.options.car.general.fast_lock.name");
    private static final Text CAR_GENERAL_FAST_LOCK_TOOLTIP = translatable("pkutils.options.car.general.fast_lock.tooltip");
    private static final Text CAR_GENERAL_HIGHLIGHT_NAME = translatable("pkutils.options.car.general.highlight.name");
    private static final Text CAR_GENERAL_HIGHLIGHT_TOOLTIP = translatable("pkutils.options.car.general.highlight.tooltip");
    private static final Text CAR_AUTOMATION_LOCK_NAME = translatable("pkutils.options.car.automation.lock.name");
    private static final Text CAR_AUTOMATION_LOCK_TOOLTIP = translatable("pkutils.options.car.automation.lock.tooltip");
    private static final Text CAR_AUTOMATION_START_NAME = translatable("pkutils.options.car.automation.start.name");
    private static final Text CAR_AUTOMATION_START_TOOLTIP = translatable("pkutils.options.car.automation.start.tooltip");

    public CarOptionsScreen(Screen parent) {
        super(parent, TEXT_CAR);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(TEXT_GENERAL, this.textRenderer), Positioner::alignHorizontalCenter);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addToggleButton(directionalLayoutWidget1, CAR_GENERAL_FAST_FIND_NAME, CAR_GENERAL_FAST_FIND_TOOLTIP, (options, value) -> options.car().fastFind(value), options -> options.car().fastFind(), 150);
        renderService.addToggleButton(directionalLayoutWidget1, CAR_GENERAL_FAST_LOCK_NAME, CAR_GENERAL_FAST_LOCK_TOOLTIP, (options, value) -> options.car().fastLock(value), options -> options.car().fastLock(), 150);

        renderService.addToggleButton(directionalLayoutWidget, CAR_GENERAL_HIGHLIGHT_NAME, CAR_GENERAL_HIGHLIGHT_TOOLTIP, (options, value) -> options.car().highlight(value), options -> options.car().highlight(), 308);

        directionalLayoutWidget.add(new TextWidget(TEXT_AUTOMATION, this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addToggleButton(directionalLayoutWidget2, CAR_AUTOMATION_LOCK_NAME, CAR_AUTOMATION_LOCK_TOOLTIP, (options, value) -> options.car().automatedLock(value), options -> options.car().automatedLock(), 150);
        renderService.addToggleButton(directionalLayoutWidget2, CAR_AUTOMATION_START_NAME, CAR_AUTOMATION_START_TOOLTIP, (options, value) -> options.car().automatedStart(value), options -> options.car().automatedStart(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
