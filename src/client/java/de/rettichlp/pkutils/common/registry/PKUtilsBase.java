package de.rettichlp.pkutils.common.registry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.lang.Boolean.getBoolean;
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
    protected static final int TEXT_BOX_MARGIN_TOP = 5;
    protected static final int TEXT_BOX_FULL_SIZE_Y = 9 /* text height */ + 2 * TEXT_BOX_PADDING + TEXT_BOX_MARGIN_TOP;

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

    public boolean isSuperUser() {
        return nonNull(player) && (player.getUuidAsString().equals("25855f4d-3874-4a7f-a6ad-e9e4f3042e19") || getBoolean("fabric.development"));
    }

    public String dateTimeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        DateTimeFormatter formatter = ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
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
        int x = client.getWindow().getScaledWidth() - textWidth - TEXT_BOX_MARGIN_TOP;
        int y = TEXT_BOX_FULL_SIZE_Y * boxIndex;

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
}
