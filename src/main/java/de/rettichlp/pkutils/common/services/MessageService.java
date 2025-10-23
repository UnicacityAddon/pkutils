package de.rettichlp.pkutils.common.services;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.player;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_PURPLE;
import static net.minecraft.util.Formatting.LIGHT_PURPLE;
import static net.minecraft.util.Formatting.WHITE;

public class MessageService {

    protected static final Text modMessagePrefix = Text.empty()
            .append(of("✦").copy().formatted(DARK_PURPLE))
            .append(of(" "))
            .append(of("PKU").copy().formatted(LIGHT_PURPLE))
            .append(of(" "))
            .append(of("|").copy().formatted(DARK_GRAY))
            .append(of(" "));

    public void sendModMessage(String message, boolean inActionbar) {
        messageService.sendModMessage(of(message).copy().formatted(WHITE), inActionbar);
    }

    public void sendModMessage(Text message, boolean inActionbar) {
        Text messageText = modMessagePrefix.copy().append(message);
        player.sendMessage(messageText, inActionbar);
    }

    public String dateTimeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        DateTimeFormatter formatter = ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public String millisToFriendlyString(long millis) {
        long totalSeconds = abs(millis) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        return (millis < 0 ? "-" : "") + (hours > 0
                ? format("%02d:%02d:%02d", hours, minutes, seconds)
                : format("%02d:%02d", minutes, seconds));
    }
}
