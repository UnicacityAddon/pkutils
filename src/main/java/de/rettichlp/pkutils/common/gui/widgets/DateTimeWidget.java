package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.messageService;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

@PKUtilsWidget(registryName = "date_time", defaultX = 4.0, defaultY = 4.0)
public class DateTimeWidget extends AbstractPKUtilsTextWidget<DateTimeWidget.Configuration> {

    @Override
    public Text text() {
        return of(messageService.dateTimeToFriendlyString(now()));
    }

    @Override
    public Text getDisplayName() {
        return translatable("pkutils.options.widgets.date_time.options.name");
    }

    @Override
    public Text getTooltip() {
        return translatable("pkutils.options.widgets.date_time.options.tooltip");
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
