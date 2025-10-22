package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.configuration.options.Options;
import de.rettichlp.pkutils.common.gui.OptionsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.translatable;

public class MainOptionsScreen extends OptionsScreen {

    private static final Text TEXT_CAR = translatable("pkutils.options.text.car");
    private static final Text TEXT_NAMETAG = translatable("pkutils.options.text.nametag");
    private static final Text TEXT_PERSONAL_USE = translatable("pkutils.options.text.personal_use");
    private static final Text TEXT_WIDGETS = translatable("pkutils.options.text.widgets");
    private static final Text REINFORCEMENT_TYPE = translatable("pkutils.options.reinforcement_type.name");
    private static final Text NOTIFICATION_SOUNDS_NAME = translatable("pkutils.options.notification_sounds.name");
    private static final Text NOTIFICATION_SOUNDS_TOOLTIP = translatable("pkutils.options.notification_sounds.tooltip");

    public MainOptionsScreen() {
        super(new GameMenuScreen(true));
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        renderService.addCyclingButton(directionalLayoutWidget, REINFORCEMENT_TYPE, Options.ReinforcementType.values(), Options.ReinforcementType::getDisplayName, Options::reinforcementType, Options::reinforcementType, 308);

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addButton(directionalLayoutWidget1, TEXT_NAMETAG, button -> this.client.setScreen(new NameTagOptionsScreen(this)), 150);
        renderService.addButton(directionalLayoutWidget1, TEXT_PERSONAL_USE, button -> this.client.setScreen(new PersonalUseOptionsScreen(this)), 150);

        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addButton(directionalLayoutWidget2, TEXT_CAR, button -> this.client.setScreen(new CarOptionsScreen(this)), 150);
        renderService.addToggleButton(directionalLayoutWidget2, NOTIFICATION_SOUNDS_NAME, NOTIFICATION_SOUNDS_TOOLTIP, Options::customSounds, Options::customSounds, 150);

        DirectionalLayoutWidget directionalLayoutWidget3 = directionalLayoutWidget.add(horizontal().spacing(8));
        renderService.addButton(directionalLayoutWidget3, TEXT_WIDGETS, button -> this.client.setScreen(new WidgetOptionsScreen(this)), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
