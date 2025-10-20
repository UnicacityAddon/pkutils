package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.gui.OptionsScreen;
import de.rettichlp.pkutils.common.gui.options.components.ToggleButtonWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.IOptionWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.translatable;

public class WidgetOptionsScreen extends OptionsScreen {

    public WidgetOptionsScreen(Screen parent) {
        super(parent, "pkutils.options.overlay.title", false);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        // general
        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.general"), this.textRenderer), Positioner::alignHorizontalCenter);

        renderService.addButton(directionalLayoutWidget, "pkutils.options.overlay.position.title", button -> this.client.setScreen(new WidgetOptionsPositionScreen(this)), 308);

        // general - enable status
        GridWidget gridWidget = directionalLayoutWidget.add(new GridWidget());
        gridWidget.setColumnSpacing(8).setRowSpacing(4);
        GridWidget.Adder gridWidgetAdder = gridWidget.createAdder(2);

        renderService.getWidgets().forEach(abstractPKUtilsWidget -> {
            Text displayName = abstractPKUtilsWidget.getDisplayName();
            ToggleButtonWidget toggleButton = new ToggleButtonWidget(displayName, value -> abstractPKUtilsWidget.getWidgetConfiguration().setEnabled(value), abstractPKUtilsWidget.getWidgetConfiguration().isEnabled());
            gridWidgetAdder.add(toggleButton);
        });

        gridWidget.refreshPositions();
        gridWidget.forEachChild(this::addDrawableChild);

        // options section per widget
        renderService.getWidgets().stream()
                .filter(abstractPKUtilsWidget -> abstractPKUtilsWidget.getWidgetConfiguration() instanceof IOptionWidget)
                .forEach(abstractPKUtilsWidget -> {
                    IOptionWidget iOptionWidget = (IOptionWidget) abstractPKUtilsWidget.getWidgetConfiguration();

                    // section title
                    Text text = iOptionWidget.sectionTitle();
                    directionalLayoutWidget.add(new TextWidget(text, this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

                    // options widget
                    directionalLayoutWidget.add(iOptionWidget.optionsWidget(), Positioner::alignHorizontalCenter);
                });

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    // disable background rendering to see overlay better
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}
