package de.rettichlp.pkutils.common.models;

import de.rettichlp.pkutils.common.gui.widgets.CountdownWidget;
import lombok.RequiredArgsConstructor;
import net.minecraft.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;

import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

@RequiredArgsConstructor
public class Countdown {

    private final LocalDateTime startTime = now();
    private final String title;
    private final Duration duration;

    public Countdown(String title, Duration duration, Runnable runAfter) {
        this.title = title;
        this.duration = duration;

        utilService.delayedAction(runAfter, this.duration.toMillis());
    }

    public boolean isActive() {
        return getRemainingDuration().isPositive();
    }

    public Duration getRemainingDuration() {
        return between(now(), this.startTime.plus(this.duration));
    }

    public CountdownWidget toWidget() {
        String millisToFriendlyString = messageService.millisToFriendlyString(getRemainingDuration().toMillis());

        Text text = empty()
                .append(of(this.title).copy().formatted(WHITE))
                .append(of(":").copy().formatted(GRAY)).append(" ")
                .append(of(millisToFriendlyString));

        return new CountdownWidget(text, this.startTime, this.duration.toMillis());
    }
}
