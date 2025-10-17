package de.rettichlp.pkutils.common.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;

import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.text.Text.empty;

public class OverlayEditScreen extends PKUtilsScreen {

    public OverlayEditScreen(Screen parent) {
        super(empty(), empty(), parent, false);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(horizontal().spacing(8), positioner -> positioner.marginTop(this.client.getWindow().getScaledHeight() / 4));

        addButton(directionalLayoutWidget, "gui.done", button -> {}, 150);
        addButton(directionalLayoutWidget, "gui.cancel", button -> back(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void doOnClose() {
        // TODO
    }

    // disable background rendering to see overlay better
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}
