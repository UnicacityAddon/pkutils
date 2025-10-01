package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.models.config.Options;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;

import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;

public class MainOptionsScreen extends OptionsScreen {

    public MainOptionsScreen() {
        super(new GameMenuScreen(true));
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));
        addButton(directionalLayoutWidget1, "pkutils.options.nametag.title", button -> this.client.setScreen(new NameTagOptionsScreen(this)), 150);

        addCyclingButton(directionalLayoutWidget, "pkutils.options.reinforcementType", Options.ReinforcementType.values(), Options.ReinforcementType::getDisplayName, Options::reinforcementType, Options::reinforcementType, 308);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }
}
