package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.gui.overlay.AlignHorizontalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.AlignVerticalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.TextOverlay;
import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.NotificationService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtilsClient.configuration;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_LEFT;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_RIGHT;
import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;

@PKUtilsListener
public class HudListener extends PKUtilsBase implements IHudRenderListener {

    private final AlignVerticalOverlay notificationOverlay = new AlignVerticalOverlay();
    private final AlignVerticalOverlay statsOverlay = new AlignVerticalOverlay();

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        renderNotifications(drawContext);
        renderStatsOverlay(drawContext);
    }

    private void renderNotifications(DrawContext drawContext) {
        this.notificationOverlay.clear();

        storage.getCountdowns().stream()
                .filter(Countdown::isActive)
                .map(Countdown::toTextWidget)
                .forEach(this.notificationOverlay::add);

        notificationService.getActiveNotifications().stream()
                .map(NotificationService.Notification::toTextOverlay)
                .forEach(this.notificationOverlay::add);

        this.notificationOverlay.draw(drawContext, TOP_RIGHT);
    }

    private void renderStatsOverlay(DrawContext drawContext) {
        this.statsOverlay.clear();

        // first row
        AlignHorizontalOverlay alignHorizontalOverlay = new AlignHorizontalOverlay();
        alignHorizontalOverlay.add(getDateTimeTextOverlay());
        alignHorizontalOverlay.add(getPayDayTextOverlay());
        alignHorizontalOverlay.add(getCarLockedOverlay());

        this.statsOverlay.add(alignHorizontalOverlay.disableMargin());

        this.statsOverlay.draw(drawContext, TOP_LEFT);
    }

    private TextOverlay getDateTimeTextOverlay() {
        return TextOverlay.builder()
                .textSupplier(() -> of(dateTimeToFriendlyString(now())))
                .build();
    }

    private TextOverlay getPayDayTextOverlay() {
        MutableText payDayInfoText = empty()
                .append(of("PayDay").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(valueOf(configuration.getMinutesSinceLastPayDay())))
                .append(of("/").copy().formatted(DARK_GRAY))
                .append(of("60")).append(" ")
                .append(of("Gehalt").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(configuration.getPredictedPayDaySalary() + "$")).append(" ")
                .append(of("Exp").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(valueOf(configuration.getPredictedPayDayExp())));

        return TextOverlay.builder()
                .textSupplier(() -> payDayInfoText)
                .build();
    }

    private TextOverlay getCarLockedOverlay() {
        boolean minimalistic = true; // TODO

        Text text = minimalistic
                ? (storage.isCarLocked() ? of("🔒").copy().formatted(GREEN) : of("🔓").copy().formatted(GOLD))
                : empty()
                .append(of("Fahrzeug").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(storage.isCarLocked() ? of("zu").copy().formatted(GREEN) : of("offen").copy().formatted(GOLD));

        return TextOverlay.builder()
                .textSupplier(() -> text)
                .build();
    }
}
