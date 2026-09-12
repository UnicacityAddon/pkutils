package de.rettichlp.ucutils.common.gui.widgets;

import de.rettichlp.therettingtoncompanion.gui.options.list.TRCOptionsList;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.AbstractTRCTextWidget;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.WidgetConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.common.services.RenderService.keyValue;
import static java.awt.Color.RED;
import static java.lang.System.currentTimeMillis;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.translatable;

public class MoneyWidget extends AbstractTRCTextWidget<MoneyWidget.Configuration> {

    @Override
    public Component text() {
        // with over 100.000$ on bank and PayDay within next 5 minutes, animate text
        return configuration.getMinutesSinceLastPayDay() >= 55 && configuration.getMoneyBankAmount() > 100000 && (currentTimeMillis() / 500 % 2 == 0)
                ? empty()
                .append(keyValue(translatable("ucutils.options.widgets.money.label_cash"), configuration.getMoneyCashAmount() + "$")).append(SPACE)
                .append(keyValue(translatable("ucutils.options.widgets.money.label_bank"), configuration.getMoneyBankAmount() + "$").withColor(RED.getRGB()))
                : empty()
                .append(keyValue(translatable("ucutils.options.widgets.money.label_cash"), configuration.getMoneyCashAmount() + "$")).append(SPACE)
                .append(keyValue(translatable("ucutils.options.widgets.money.label_bank"), configuration.getMoneyBankAmount() + "$"));
    }

    @Override
    public @Nullable String getRegistryName() {
        return "money";
    }

    @Override
    public Component getLabel() {
        return translatable("ucutils.options.widgets.money.options.name");
    }

    @Override
    public Component getTooltip() {
        return translatable("ucutils.options.widgets.money.options.tooltip");
    }

    @Override
    public void addOptions(@NonNull TRCOptionsList optionsList) {}

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends WidgetConfiguration {}
}
