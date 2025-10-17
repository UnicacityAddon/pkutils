package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import lombok.AllArgsConstructor;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static net.minecraft.text.Text.empty;

@PKUtilsWidget(registryName = "money")
public class MoneyWidget extends AbstractPKUtilsTextWidget<MoneyWidget.Configuration> {

    @Override
    public Text text() {
        return empty()
                .append(keyValue("Geld", configuration.getMoneyCashAmount() + "$"))
                .append(keyValue("Bank", configuration.getMoneyBankAmount() + "$"));
    }

    @AllArgsConstructor
    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
