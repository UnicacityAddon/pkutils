package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsProgressTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import lombok.RequiredArgsConstructor;
import net.minecraft.text.Text;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static net.minecraft.text.Text.empty;

@RequiredArgsConstructor
@PKUtilsWidget(registryName = "countdown")
public class CountdownWidget extends AbstractPKUtilsProgressTextWidget<CountdownWidget.Configuration> {

    private final Text text;
    private final LocalDateTime creationTime;
    private final long durationInMillis;

    @Override
    public Text text() {
        return this.text;
    }

    @Override
    public double progress() {
        return calculateProgress(this.creationTime, this.durationInMillis);
    }

    @Override
    public Text getDisplayName() {
        return empty();
    }

    @Override
    public Text getTooltip() {
        return empty();
    }

    @Override
    public boolean isVisible() {
        return this.creationTime.plus(this.durationInMillis, MILLIS).isAfter(now());
    }

    public static class Configuration extends PKUtilsWidgetConfiguration {}
}
