package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static de.rettichlp.pkutils.common.services.HudService.NotificationType.DEFAULT;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class HudService extends PKUtilsBase {

    private final Collection<Notification> notifications = new ArrayList<>();

    public void sendNotification(String message) {
        sendNotification(message, DEFAULT);
    }

    public void sendNotification(String message, @NotNull NotificationType notificationType) {
        Notification notification = new Notification(Text.of(message), 5000);
        Notification styledNotification = notificationType.apply(notification);
        this.notifications.add(styledNotification);
    }

    public List<Notification> getActiveNotifications() {
        return this.notifications.stream()
                .filter(notification -> now().isBefore(notification.getTimestamp().plus(notification.getDurationInMillis(), MILLISECONDS.toChronoUnit())))
                .sorted(Comparator.comparing(HudService.Notification::getTimestamp))
                .toList();
    }

    @Data
    public static class Notification {

        private final Text text;
        private final long durationInMillis;
        private final LocalDateTime timestamp = now();
        private int borderColor = 0xFFFFFFFF;
        private int backgroundColor = 0xAA000000;
    }

    @Getter
    @AllArgsConstructor
    public enum NotificationType {

        ACTIVITY(new Color(255, 0, 204, 255), new Color(132, 30, 106, 100)),
        DEFAULT(new Color(255, 255, 255, 255), new Color(0, 0, 0, 100)),
        WARNING(new Color(255, 153, 0, 255), new Color(132, 81, 20, 100)),
        ERROR(new Color(255, 0, 0, 255), new Color(133, 28, 11, 100));

        private final Color borderColor;
        private final Color backgroundColor;

        @Contract("_ -> param1")
        public @NotNull Notification apply(@NotNull Notification notification) {
            notification.setBorderColor(this.borderColor.getRGB());
            notification.setBackgroundColor(this.backgroundColor.getRGB());
            return notification;
        }
    }
}
