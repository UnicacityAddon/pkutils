package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.config.OverlayOptions.CarLockedStyle.MINIMALISTIC;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;

@PKUtilsWidget(registryName = "car_locked")
public class CarLockedWidget extends AbstractPKUtilsTextWidget<CarLockedWidget.Configuration> {

    @Override
    public Text text() {
        boolean minimalistic = configuration.getOptions().overlay().carLockedStyle() == MINIMALISTIC;

        return minimalistic
                ? (storage.isCarLocked() ? of("🔒").copy().formatted(GREEN) : of("🔓").copy().formatted(GOLD))
                : empty()
                .append(of("Fahrzeug").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(storage.isCarLocked() ? of("zu").copy().formatted(GREEN) : of("offen").copy().formatted(GOLD));
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
