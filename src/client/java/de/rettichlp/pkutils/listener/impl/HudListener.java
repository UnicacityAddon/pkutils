package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.gui.overlay.AlignHorizontalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.AlignVerticalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.TextOverlay;
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
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_LEFT;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_RIGHT;
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

    private final AlignVerticalOverlay notificationOverlay = new AlignVerticalOverlay();
    private final AlignVerticalOverlay statsOverlay = new AlignVerticalOverlay();

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        renderCountdowns(drawContext);
        renderNotifications(drawContext);
        renderStatsOverlay(drawContext);
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

    private void renderNotifications(DrawContext drawContext) {
        this.notificationOverlay.clear();

        notificationService.getActiveNotifications().stream()
                .map(NotificationService.Notification::toTextOverlay)
                .forEach(this.notificationOverlay::add);

        this.notificationOverlay.draw(drawContext, TOP_RIGHT);
    }

    private void renderStatsOverlay(DrawContext drawContext) {
        this.statsOverlay.clear();

        TextOverlay dateTimeTextOverlay = TextOverlay.builder()
                .textSupplier(() -> of(dateTimeToFriendlyString(now())))
                .build();

        // first row
        AlignHorizontalOverlay alignHorizontalOverlay = new AlignHorizontalOverlay();
        alignHorizontalOverlay.add(dateTimeTextOverlay);

        this.statsOverlay.add(alignHorizontalOverlay.disableMargin());

        this.statsOverlay.draw(drawContext, TOP_LEFT);
    }
}
