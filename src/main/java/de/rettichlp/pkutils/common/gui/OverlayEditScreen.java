package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.text.Text.empty;

public class OverlayEditScreen extends PKUtilsScreen {

    public OverlayEditScreen(Screen parent) {
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

    // disable background rendering to see overlay better
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean b = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        renderService.getWidgets().stream()
                .filter(abstractPKUtilsWidget -> abstractPKUtilsWidget.isMouseOver(mouseX, mouseY))
                .findFirst()
                .ifPresent(abstractPKUtilsWidget -> {
                    PKUtilsWidgetConfiguration widgetConfiguration = abstractPKUtilsWidget.getWidgetConfiguration();
                    widgetConfiguration.setX(widgetConfiguration.getX() + (int) deltaX);
                    widgetConfiguration.setY(widgetConfiguration.getY() + (int) deltaY);
                    abstractPKUtilsWidget.saveConfiguration();
                });

        return b;
    }
}
