package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.NotificationService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.renderService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.services.RenderService.TextBoxPosition.TOP_LEFT;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toMap;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

@PKUtilsListener
public class HudListener extends PKUtilsBase implements IHudRenderListener {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        renderCountdowns(drawContext);
        renderDateTime(drawContext);
        renderNotifications(drawContext);
    }

    private void renderCountdowns(DrawContext drawContext) {
        Map<Storage.Countdown, Long> activeCountdownRemainingMillis = storage.getCountdowns().entrySet().stream()
                .filter(cooldownLocalDateTimeEntry -> {
                    LocalDateTime cooldownStartTime = cooldownLocalDateTimeEntry.getValue();
                    Storage.Countdown countdown = cooldownLocalDateTimeEntry.getKey();
                    LocalDateTime countdownExpiredTime = cooldownStartTime.plus(countdown.getDuration());
                    return now().isBefore(countdownExpiredTime); // the countdown is active
                })
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> between(now(), entry.getValue().plus(entry.getKey().getDuration())).toMillis()
                ));

        if (activeCountdownRemainingMillis.isEmpty()) {
            return;
        }

        // build countdown strings
        List<MutableText> countdownStrings = activeCountdownRemainingMillis.entrySet().stream()
                .map(entry -> {
                    String millisToFriendlyString = millisToFriendlyString(entry.getValue());
                    return empty()
                            .append(of(entry.getKey().getDisplayName()).copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of(millisToFriendlyString));
                })
                .toList();

        // build text box
        MutableText countdownText = empty();
        countdownStrings.forEach(mutableText -> countdownText.append(mutableText).append(" "));

        renderService.renderTextBox(
                drawContext,
                countdownText,
                new Color(127, 127, 127, 100),
                new Color(255, 255, 255, 255),
                0);
    }

    private void renderDateTime(DrawContext drawContext) {
        String dateTimeToFriendlyString = dateTimeToFriendlyString(now());
        renderService.renderTextBox(drawContext, of(dateTimeToFriendlyString), TOP_LEFT);
    }

    private void renderNotifications(DrawContext drawContext) {
        List<NotificationService.Notification> activeNotifications = notificationService.getActiveNotifications();

        if (activeNotifications.isEmpty()) {
            return;
        }

        Map<NotificationService.Notification, Integer> notificationIndexes = activeNotifications.stream()
                .collect(toMap(notification -> notification, activeNotifications::indexOf));

        notificationIndexes.forEach((notification, notificationIndex) -> renderService.renderTextBox(
                drawContext,
                notification.getTextSupplier().get(),
                notification.getBackgroundColor(),
                notification.getBorderColor(),
                notificationIndex + 1)); // +1 because placeholder for countdowns
    }
}
