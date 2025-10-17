package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import de.rettichlp.pkutils.common.models.config.OverlayOptions;
import lombok.AllArgsConstructor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static java.lang.String.valueOf;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;

@PKUtilsWidget(registryName = "payday")
public class PayDayWidget extends AbstractPKUtilsTextWidget<PayDayWidget.Configuration> {

    @Override
    public Text text() {
        OverlayOptions overlayOptions = configuration.getOptions().overlay(); // TODO to configuration

        MutableText payDayInfoText = keyValue("PayDay", empty()
                .append(of(valueOf(configuration.getMinutesSinceLastPayDay())))
                .append(of("/").copy().formatted(DARK_GRAY))
                .append(of("60")));

        if (overlayOptions.payDaySalary()) {
            payDayInfoText.append(" ").append(keyValue("Gehalt", configuration.getPredictedPayDaySalary() + "$"));
        }

        if (overlayOptions.payDayExperience()) {
            payDayInfoText.append(" ").append(keyValue("Exp", valueOf(configuration.getPredictedPayDayExp())));
        }

        return payDayInfoText;
    }

    @AllArgsConstructor
    public static class Configuration extends PKUtilsWidgetConfiguration {

        private final boolean showSalary = true;
        private final boolean showExperience = true;
    }
}
