package de.rettichlp.ucutils.common.models;

import de.rettichlp.therettingtoncompanion.models.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

import static de.rettichlp.ucutils.UCUtils.messageService;
import static de.rettichlp.ucutils.UCUtils.notificationService;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.WHITE;

@Getter
@RequiredArgsConstructor
public class Countdown {

    private final LocalDateTime startTime = now();
    private final String title;
    private final Duration duration;

    public Countdown(String title, Duration duration, Runnable runAfter) {
        this.title = title;
        this.duration = duration;

        Notification countdownNotification = Notification.builder()
                .componentSupplier(() -> {
                    String millisToFriendlyString = messageService.millisToFriendlyString(getRemainingDuration().toMillis());

                    return empty()
                            .append(literal(this.title).withColor(WHITE))
                            .append(literal(":").withColor(GRAY)).append(" ")
                            .append(literal(millisToFriendlyString));
                })
                .displayDuration(duration)
                .build();

        storage.getCountdowns().add(this);
        notificationService.getNotifications().add(countdownNotification);
        utilService.delayedAction(runAfter, this.duration.toMillis());
    }

    public Duration getRemainingDuration() {
        return between(now(), this.startTime.plus(this.duration));
    }
}
