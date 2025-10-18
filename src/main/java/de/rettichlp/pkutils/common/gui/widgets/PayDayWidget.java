package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static java.lang.String.valueOf;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;

@PKUtilsWidget(registryName = "payday", defaultX = 126.0, defaultY = 4.0)
public class PayDayWidget extends AbstractPKUtilsTextWidget<PayDayWidget.Configuration> {

    @Override
    public Text text() {
        MutableText payDayInfoText = keyValue("PayDay", empty()
                .append(of(valueOf(configuration.getMinutesSinceLastPayDay())))
                .append(of("/").copy().formatted(DARK_GRAY))
                .append(of("60")));

        if (getWidgetConfiguration().isShowSalary()) {
            payDayInfoText.append(" ").append(keyValue("Gehalt", configuration.getPredictedPayDaySalary() + "$"));
        }

        if (getWidgetConfiguration().isShowExperience()) {
            payDayInfoText.append(" ").append(keyValue("Exp", valueOf(configuration.getPredictedPayDayExp())));
        }

        return payDayInfoText;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends PKUtilsWidgetConfiguration {

        private boolean showSalary = true;
        private boolean showExperience = true;
    }
}
