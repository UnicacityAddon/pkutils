package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.gui.PKUtilsScreen;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_PADDING;
import static java.awt.Color.BLACK;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.text.Text.empty;

public class WidgetOptionsPositionScreen extends PKUtilsScreen {

    private String widgetLocationText = "";

    public WidgetOptionsPositionScreen(Screen parent) {
        super(empty(), empty(), parent, false);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(horizontal().spacing(8), positioner -> positioner.marginTop(this.client.getWindow().getScaledHeight() / 4));

        addButton(directionalLayoutWidget, "gui.done", button -> {
            configuration.updateWidgetConfigurations();
            back();
        }, 150);

        addButton(directionalLayoutWidget, "gui.cancel", button -> {
            // restore configurations from the configuration file
            renderService.getWidgets().forEach(abstractPKUtilsWidget -> {
                try {
                    abstractPKUtilsWidget.loadConfiguration();
                } catch (Exception e) {
                    notificationService.sendErrorNotification("Konfiguration konnte nicht wiederhergestellt werden");
                    LOGGER.error("Could not restore configuration for widget {}", abstractPKUtilsWidget.getClass().getName(), e);
                }
            });

            back();
        }, 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void doOnClose() {
        configuration.saveToFile();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (!this.widgetLocationText.isBlank()) {
            int textX = mouseX + 10;
            int textY = mouseY + 10;

            Text widgetLocationText = Text.of(this.widgetLocationText);
            context.fill(textX - TEXT_BOX_PADDING, textY - TEXT_BOX_PADDING, textX + this.textRenderer.getWidth(widgetLocationText) + TEXT_BOX_PADDING, textY + this.textRenderer.fontHeight + TEXT_BOX_PADDING, renderService.getSecondaryColor(BLACK).getRGB());
            context.drawText(this.textRenderer, widgetLocationText, textX, textY, 0xFFFFFF, false);
        }
    }

    // disable background rendering to see overlay better
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean mouseDragged = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        this.widgetLocationText = "";

        renderService.getWidgets().stream()
                .filter(abstractPKUtilsWidget -> abstractPKUtilsWidget.isMouseOver(mouseX, mouseY))
                .findFirst()
                .ifPresent(abstractPKUtilsWidget -> {
                    PKUtilsWidgetConfiguration widgetConfiguration = abstractPKUtilsWidget.getWidgetConfiguration();

                    double speedFactor = 1.2; // it's a bit too slow without the factor
                    int newX = widgetConfiguration.getX() + (int) (deltaX * speedFactor);
                    int newY = widgetConfiguration.getY() + (int) (deltaY * speedFactor);

                    widgetConfiguration.setX(newX);
                    widgetConfiguration.setY(newY);
                    abstractPKUtilsWidget.saveConfiguration();

                    this.widgetLocationText = "X: " + newX + " Y: " + newY;
                });

        return mouseDragged;
    }
}
