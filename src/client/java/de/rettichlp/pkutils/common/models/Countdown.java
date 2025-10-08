package de.rettichlp.pkutils.common.models;

import de.rettichlp.pkutils.common.gui.overlay.TextOverlay;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.RequiredArgsConstructor;
import net.minecraft.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

@RequiredArgsConstructor
public class Countdown extends PKUtilsBase {

    private final LocalDateTime startTime = now();
    private final String title;
    private final Duration duration;

    public boolean isActive() {
        return getRemainingDuration().isPositive();
    }

    public Duration getRemainingDuration() {
        return between(now(), this.startTime.plus(this.duration));
    }

    public TextOverlay toTextWidget() {
        String millisToFriendlyString = millisToFriendlyString(getRemainingDuration().toMillis());

        Text text = empty()
                .append(of(this.title).copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(of(millisToFriendlyString));

        return TextOverlay.builder()
                .textSupplier(() -> text)
                .build();
    }
}
