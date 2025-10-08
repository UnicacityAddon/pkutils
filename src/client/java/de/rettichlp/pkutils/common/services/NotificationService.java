package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.gui.overlay.TextOverlay;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Data;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.time.LocalDateTime.now;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NotificationService extends PKUtilsBase {

    private final Collection<Notification> notifications = new ArrayList<>();

    public void sendSuccessNotification(String message) {
        sendNotification(message, GREEN, 5000);
    }

    public void sendInfoNotification(String message) {
        sendNotification(message, CYAN, 5000);
    }

    public void sendWarningNotification(String message) {
        sendNotification(message, ORANGE, 5000);
    }

    public void sendErrorNotification(String message) {
        sendNotification(message, RED, 5000);
    }

    public void sendNotification(String message, Color color, long durationInMillis) {
        sendNotification(() -> Text.of(message), color, durationInMillis);
    }

    public void sendNotification(@NotNull Supplier<Text> messageSupplier, Color color, long durationInMillis) {
        Notification notification = new Notification(messageSupplier, durationInMillis);
        notification.setBorderColor(color);
        notification.setBackgroundColor(new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2, 100));
        this.notifications.add(notification);
    }

    public List<Notification> getActiveNotifications() {
        return this.notifications.stream()
                .filter(notification -> now().isBefore(notification.getTimestamp().plus(notification.getDurationInMillis(), MILLISECONDS.toChronoUnit())))
                .sorted(Comparator.comparing(NotificationService.Notification::getTimestamp))
                .toList();
    }

    @Data
    public static class Notification {

        private final UUID id = randomUUID();
        private final Supplier<Text> textSupplier;
        private final long durationInMillis;
        private final LocalDateTime timestamp = now();
        private Color borderColor = new Color(255, 255, 255, 255);
        private Color backgroundColor = new Color(127, 127, 127, 100);

        @Override
        public int hashCode() {
            return hash(this.id, this.textSupplier, this.durationInMillis, this.timestamp, this.borderColor, this.backgroundColor);
        }

        @Override
        public boolean equals(Object o) {
            return nonNull(o) && o instanceof Notification that && Objects.equals(this.id, that.id);
        }

        public TextOverlay toTextOverlay() {
            return TextOverlay.builder()
                    .textSupplier(this.textSupplier)
                    .backgroundColor(this.backgroundColor)
                    .borderColor(this.borderColor)
                    .build();
        }
    }
}
