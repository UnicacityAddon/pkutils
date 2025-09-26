package de.rettichlp.pkutils.common.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.nonNull;
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

    protected static final int TEXT_BOX_PADDING = 3;
    protected static final int TEXT_BOX_MARGIN = 5;
    protected static final int TEXT_BOX_FULL_SIZE_Y = 9 /* text height */ + 2 * TEXT_BOX_PADDING + TEXT_BOX_MARGIN;

    public void sendCommand(String command) {
        LOGGER.info("PKUtils executing command: {}", command);
        networkHandler.sendChatCommand(command);
    }

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
                MinecraftClient.getInstance().execute(runnable);
            }
        }, milliseconds);
    }

    public String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new NullPointerException("Cannot find version"));
    }

    public boolean isSuperUser() {
        return nonNull(player) && (player.getUuidAsString().equals("25855f4d-3874-4a7f-a6ad-e9e4f3042e19") || getBoolean("fabric.development"));
    }

    public String dateTimeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        DateTimeFormatter formatter = ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public String millisToFriendlyString(long millis) {
        long totalSeconds = millis / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        return hours > 0
                ? format("%02d:%02d:%02d", hours, minutes, seconds)
                : format("%02d:%02d", minutes, seconds);
    }
}
