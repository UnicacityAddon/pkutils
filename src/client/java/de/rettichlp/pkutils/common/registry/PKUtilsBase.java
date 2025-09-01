package de.rettichlp.pkutils.common.registry;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.time.format.DateTimeFormatter.ofPattern;
import static net.minecraft.client.MinecraftClient.getInstance;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_PURPLE;
import static net.minecraft.util.Formatting.LIGHT_PURPLE;
import static net.minecraft.util.Formatting.WHITE;

public abstract class PKUtilsBase {

    protected static final Text modMessagePrefix = Text.empty()
            .append(of("✦").copy().formatted(DARK_PURPLE))
            .append(of(" "))
            .append(of("PKU").copy().formatted(LIGHT_PURPLE))
            .append(of(" "))
            .append(of("|").copy().formatted(DARK_GRAY))
            .append(of(" "));

    public void sendModMessage(String message, boolean inActionbar) {
        sendModMessage(of(message).copy().formatted(WHITE), inActionbar);
    }

    public void sendModMessage(Text message, boolean inActionbar) {
        Text messageText = modMessagePrefix.copy().append(message);
        player.sendMessage(messageText, inActionbar);
    }

    public void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getInstance().execute(runnable);
            }
        }, milliseconds);
    }

    public boolean isSuperUser() {
        String uuidAsString = player.getUuidAsString();
        return uuidAsString.equals("25855f4d-3874-4a7f-a6ad-e9e4f3042e19");
    }

    public String dateTimeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        DateTimeFormatter formatter = ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }
}
