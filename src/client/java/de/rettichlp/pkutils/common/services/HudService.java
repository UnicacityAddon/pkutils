package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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

import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.time.LocalDateTime.now;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class HudService extends PKUtilsBase {

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
        Notification notification = new Notification(Text.of(message), durationInMillis);
        notification.setBorderColor(color);
        notification.setBackgroundColor(new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2, 100));
        this.notifications.add(notification);
    }

    public List<Notification> getActiveNotifications() {
        return this.notifications.stream()
                .filter(notification -> now().isBefore(notification.getTimestamp().plus(notification.getDurationInMillis(), MILLISECONDS.toChronoUnit())))
                .sorted(Comparator.comparing(HudService.Notification::getTimestamp))
                .toList();
    }

    public void renderTextBox(@NotNull DrawContext drawContext,
                              Text text,
                              @NotNull Color backgroundColor,
                              @NotNull Color borderColor,
                              int boxIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;
        int x = client.getWindow().getScaledWidth() - textWidth - TEXT_BOX_MARGIN;
        int y = TEXT_BOX_FULL_SIZE_Y * boxIndex + TEXT_BOX_MARGIN;

        drawContext.fill(
                x - TEXT_BOX_PADDING,
                y - TEXT_BOX_PADDING,
                x + textWidth + TEXT_BOX_PADDING,
                y + textHeight + TEXT_BOX_PADDING,
                backgroundColor.getRGB()
        );

        drawContext.drawBorder(
                x - TEXT_BOX_PADDING,
                y - TEXT_BOX_PADDING,
                textWidth + TEXT_BOX_PADDING * 2,
                textHeight + TEXT_BOX_PADDING * 2,
                borderColor.getRGB()
        );

        drawContext.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
    }

    @Data
    public static class Notification {

        private final UUID id = randomUUID();
        private final Text text;
        private final long durationInMillis;
        private final LocalDateTime timestamp = now();
        private Color borderColor = new Color(255, 255, 255, 255);
        private Color backgroundColor = new Color(127, 127, 127, 100);

        @Override
        public boolean equals(Object o) {
            return nonNull(o) && o instanceof Notification that && Objects.equals(this.id, that.id);
        }

        @Override
        public int hashCode() {
            return hash(this.id, this.text, this.durationInMillis, this.timestamp, this.borderColor, this.backgroundColor);
        }
    }
}
