package de.rettichlp.pkutils.common.api.response;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.temporal.TemporalAdjusters.nextOrSame;

public record WeeklyTime(DayOfWeek dayOfWeek, LocalTime time) {

    public @NotNull LocalDateTime nextOccurrence() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now
                .with(nextOrSame(this.dayOfWeek))
                .with(this.time);

        if (next.isBefore(now)) {
            next = next.plusWeeks(1);
        }

        return next;
    }
}
