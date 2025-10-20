package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.text.Text;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.storage;
import static java.awt.Color.RED;
import static java.lang.String.valueOf;

@PKUtilsWidget(registryName = "service_count", defaultX = 4.0, defaultY = 42.0)
public class ServiceCountWidget extends AbstractPKUtilsTextWidget<ServiceCountWidget.Configuration> {

    @Override
    public Text text() {
        return keyValue("Services", valueOf(storage.getActiveServices()));
    }

    @Override
    public Color getBorderColor() {
        return RED;
    }

    @Override
    public boolean isVisible() {
        return storage.getActiveServices() > 0;
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
