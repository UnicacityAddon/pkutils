package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

public class PlayerListener implements IAbsorptionGetListener, IMessageReceiveListener, ITickListener {

    // afk
    private static final Pattern AFK_START_PATTERN = compile("^Du bist nun im AFK-Modus\\.$");
    private static final Pattern AFK_END_PATTERN = compile("^Du bist nun nicht mehr im AFK-Modus\\.$");

    private boolean isAfk = false;

    @Override
    public void onAbsorptionGet() {
        storage.getCountdowns().add(new Countdown("Absorption", ofMinutes(3)));
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher afkStartMatcher = AFK_START_PATTERN.matcher(message);
        if (afkStartMatcher.find()) {
            this.isAfk = true;
            return true;
        }

        Matcher afkEndMatcher = AFK_END_PATTERN.matcher(message);
        if (afkEndMatcher.find()) {
            this.isAfk = false;
            return true;
        }

        return true;
    }

    @Override
    public void onTick() {
        if (player.age % 1200 == 0 && !this.isAfk) {
            configService.edit(mainConfig -> mainConfig.addMinutesSinceLastPayDay(1));
        }
    }
}
