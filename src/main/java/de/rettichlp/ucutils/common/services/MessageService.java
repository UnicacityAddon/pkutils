package de.rettichlp.ucutils.common.services;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.rettichlp.ucutils.UCUtils.messageService;
import static de.rettichlp.ucutils.UCUtils.player;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.DARK_PURPLE;
import static net.minecraft.network.chat.TextColor.LIGHT_PURPLE;
import static net.minecraft.network.chat.TextColor.WHITE;

public class MessageService {

    public static final DateTimeFormatter DATE_TIME_FORMAT = ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMAT = ofPattern("HH:mm:ss");

    protected static final MutableComponent modMessagePrefix = Component.empty()
            .append(literal("✦").withColor(DARK_PURPLE))
            .append(literal(" "))
            .append(literal("UCU").withColor(LIGHT_PURPLE))
            .append(literal(" "))
            .append(literal("|").withColor(DARK_GRAY))
            .append(literal(" "));

    public void sendModMessage(String message, boolean inActionbar) {
        messageService.sendModMessage(literal(message).withColor(WHITE), inActionbar);
    }

    public void sendModMessage(Component message, boolean inActionbar) {
        Component messageText = modMessagePrefix.copy().append(message);

        if (inActionbar) {
            player.sendOverlayMessage(messageText);
        } else {
            player.sendSystemMessage(messageText);
        }
    }

    public String dateTimeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        return dateTime.format(DATE_TIME_FORMAT);
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
