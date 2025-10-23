package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.screens.options.WidgetOptionsPositionScreen;
import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.storage;
import static java.awt.Color.RED;
import static java.lang.String.valueOf;

@PKUtilsWidget(registryName = "service_count", defaultX = 4.0, defaultY = 42.0)
public class ServiceCountWidget extends AbstractPKUtilsTextWidget<ServiceCountWidget.Configuration> {

    private static final Text WIDGETS_SERVICE_COUNT_OPTIONS_NAME = Text.translatable("pkutils.options.widgets.service_count.options.name");
    private static final Text WIDGETS_SERVICE_COUNT_OPTIONS_TOOLTIP = Text.translatable("pkutils.options.widgets.service_count.options.tooltip");

    @Override
    public Text text() {
        return keyValue("Services", valueOf(storage.getActiveServices()));
    }

    @Override
    public Color getBorderColor() {
        return RED;
    }

    @Override
    public Text getDisplayName() {
        return WIDGETS_SERVICE_COUNT_OPTIONS_NAME;
    }

    @Override
    public Text getTooltip() {
        return WIDGETS_SERVICE_COUNT_OPTIONS_TOOLTIP;
    }

    @Override
    public boolean isVisible() {
        // visible if in the position options screen to allow positioning
        return storage.getActiveServices() > 0 || MinecraftClient.getInstance().currentScreen instanceof WidgetOptionsPositionScreen;
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
