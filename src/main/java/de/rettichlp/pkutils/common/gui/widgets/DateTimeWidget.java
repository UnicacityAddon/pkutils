package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import net.minecraft.text.Text;

import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.of;

@PKUtilsWidget(registryName = "date_time")
public class DateTimeWidget extends AbstractPKUtilsTextWidget<DateTimeWidget.Configuration> {

    @Override
    public Text text() {
        return of(dateTimeToFriendlyString(now()));
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
