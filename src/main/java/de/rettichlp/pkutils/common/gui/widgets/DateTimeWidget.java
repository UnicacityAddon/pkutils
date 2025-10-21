package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.messageService;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.of;

@PKUtilsWidget(registryName = "date_time", defaultX = 4.0, defaultY = 4.0)
public class DateTimeWidget extends AbstractPKUtilsTextWidget<DateTimeWidget.Configuration> {

    @Override
    public Text text() {
        return of(messageService.dateTimeToFriendlyString(now()));
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
