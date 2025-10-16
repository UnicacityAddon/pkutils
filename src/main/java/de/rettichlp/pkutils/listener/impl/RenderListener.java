package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.gui.overlay.AlignHorizontalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.AlignVerticalOverlay;
import de.rettichlp.pkutils.common.gui.overlay.TextOverlay;
import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.models.config.OverlayOptions;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.NotificationService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_LEFT;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.DrawPosition.TOP_RIGHT;
import static de.rettichlp.pkutils.common.models.config.OverlayOptions.CarLockedStyle.MINIMALISTIC;
import static java.awt.Color.RED;
import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;

@PKUtilsListener
public class RenderListener extends PKUtilsBase implements IHudRenderListener {

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
                .map(NotificationService.Notification::toProgressTextOverlay)
                .forEach(this.notificationOverlay::add);

        this.notificationOverlay.draw(drawContext, TOP_RIGHT);
    }

    private void renderStatsOverlay(DrawContext drawContext) {
        OverlayOptions overlayOptions = configuration.getOptions().overlay();
        this.statsOverlay.clear();

        // first row
        AlignHorizontalOverlay alignHorizontalOverlay = new AlignHorizontalOverlay();

        if (overlayOptions.dateTime()) {
            alignHorizontalOverlay.add(getDateTimeTextOverlay());
        }

        if (overlayOptions.payDay()) {
            alignHorizontalOverlay.add(getPayDayTextOverlay());
        }

        if (overlayOptions.carLocked()) {
            alignHorizontalOverlay.add(getCarLockedOverlay());
        }

        // second row
        AlignHorizontalOverlay alignHorizontalOverlay1 = new AlignHorizontalOverlay();

        if (overlayOptions.money()) {
            alignHorizontalOverlay1.add(getMoneyTextOverlay());
        }

        // third row
        AlignHorizontalOverlay alignHorizontalOverlay2 = new AlignHorizontalOverlay();

        if (overlayOptions.serviceCount() && storage.getActiveServices() > 0) {
            alignHorizontalOverlay2.add(getServiceCountTextOverlay());
        }

        this.statsOverlay.add(alignHorizontalOverlay.disableMargin());
        this.statsOverlay.add(alignHorizontalOverlay1.disableMargin());
        this.statsOverlay.add(alignHorizontalOverlay2.disableMargin());
        this.statsOverlay.draw(drawContext, TOP_LEFT);
    }

    private TextOverlay getDateTimeTextOverlay() {
        return TextOverlay.builder()
                .textSupplier(() -> of(dateTimeToFriendlyString(now())))
                .build();
    }

    private TextOverlay getPayDayTextOverlay() {
        OverlayOptions overlayOptions = configuration.getOptions().overlay();

        MutableText payDayInfoText = empty()
                .append(of("PayDay").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(valueOf(configuration.getMinutesSinceLastPayDay())))
                .append(of("/").copy().formatted(DARK_GRAY))
                .append(of("60"));

        if (overlayOptions.payDaySalary()) {
            payDayInfoText.append(" ")
                    .append(of("Gehalt").copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(configuration.getPredictedPayDaySalary() + "$"));
        }

        if (overlayOptions.payDayExperience()) {
            payDayInfoText.append(" ")
                    .append(of("Exp").copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(valueOf(configuration.getPredictedPayDayExp())));
        }

        return TextOverlay.builder()
                .textSupplier(() -> payDayInfoText)
                .build();
    }

    private TextOverlay getCarLockedOverlay() {
        boolean minimalistic = configuration.getOptions().overlay().carLockedStyle() == MINIMALISTIC;

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

    private TextOverlay getMoneyTextOverlay() {
        MutableText moneyInfoText = empty()
                .append(of("Geld").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(configuration.getMoneyCashAmount() + "$")).append(" ")
                .append(of("Bank").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(configuration.getMoneyBankAmount() + "$")).append(" ");

        return TextOverlay.builder()
                .textSupplier(() -> moneyInfoText)
                .build();
    }

    private TextOverlay getServiceCountTextOverlay() {
        MutableText serviceCountText = empty()
                .append(of("Services").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(valueOf(storage.getActiveServices())));

        return TextOverlay.builder()
                .textSupplier(() -> serviceCountText)
                .backgroundColor(renderService.getSecondaryColor(RED))
                .borderColor(RED)
                .build();
    }
}
