package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Faction.FBI;
import static de.rettichlp.pkutils.common.models.Faction.POLIZEI;
import static de.rettichlp.pkutils.common.models.Faction.RETTUNGSDIENST;
import static de.rettichlp.pkutils.common.models.Sound.BOMB_SOUND;
import static java.lang.String.format;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.literal;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.RED;

@PKUtilsListener
public class BombListener extends PKUtilsBase implements IMessageReceiveListener, IHudRenderListener {

    private static final Pattern BOMB_FOUND_PATTERN = compile("^News: ACHTUNG! Es wurde eine Bombe in der Nähe von (?<location>.+) gefunden!$");
    private static final Pattern BOMB_STOP_PATTERN = compile("^News: Die Bombe konnte (erfolgreich|nicht) entschärft werden!$");

    private LocalDateTime bombPlantedTime = null;
    private String bombLocationString = "";

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Faction playerFaction = storage.getFaction(player.getName().getString());
        if (playerFaction != POLIZEI && playerFaction != FBI && playerFaction != RETTUNGSDIENST) {
            return true;
        }

        Matcher bombFoundMatcher = BOMB_FOUND_PATTERN.matcher(message);
        if (bombFoundMatcher.find()) {
            this.bombPlantedTime = now();
            this.bombLocationString = bombFoundMatcher.group("location");
            BOMB_SOUND.play();
            return true;
        }

        Matcher bombStopMatcher = BOMB_STOP_PATTERN.matcher(message);
        if (bombStopMatcher.find()) {
            this.bombPlantedTime = null;
            this.bombLocationString = "";
            return true;
        }

        return true;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (isNull(this.bombPlantedTime)) {
            return;
        }

        long elapsedTimeInMillis = between(this.bombPlantedTime, now()).toMillis();
        long minutes = MILLISECONDS.toMinutes(elapsedTimeInMillis);
        long seconds = MILLISECONDS.toSeconds(elapsedTimeInMillis) % 60;

        Text timerText = empty()
                .append(literal("Bombe").formatted(RED))
                .append(literal(":").formatted(GRAY)).append(" ")
                .append(literal(this.bombLocationString).formatted(GOLD)).append(" ")
                .append(literal("|").formatted(GRAY)).append(" ")
                .append(literal(format("%02d:%02d", minutes, seconds)).formatted(RED, BOLD));

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int textWidth = textRenderer.getWidth(timerText);
        int x = (client.getWindow().getScaledWidth() - textWidth) / 2;
        int y = 15;

        drawContext.drawTextWithShadow(textRenderer, timerText, x, y, 0xFFFFFF);
    }
}
