package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.lang.Integer.parseInt;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

public class PlayerListener extends PKUtilsBase implements IAbsorptionGetListener, IMessageReceiveListener, ITickListener {

    // afk
    private static final Pattern AFK_START_PATTERN = compile("^Du bist nun im AFK-Modus\\.$");
    private static final Pattern AFK_END_PATTERN = compile("^Du bist nun nicht mehr im AFK-Modus\\.$");

    // dead
    private static final Pattern DEAD_PATTERN = compile("^Du bist nun für (?<minutes>\\d+) Minuten auf dem Friedhof\\.$");

    // jail
    private static final Pattern JAIL_PATTERN = compile("^\\[Gefängnis] Du bist nun für (?<minutes>\\d+) Minuten im Gefängnis\\.$");

    @Override
    public void onAbsorptionGet() {
        storage.getCountdowns().add(new Countdown("Absorption", ofMinutes(3)));
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher afkStartMatcher = AFK_START_PATTERN.matcher(message);
        if (afkStartMatcher.find()) {
            storage.setAfk(true);
            return true;
        }

        Matcher afkEndMatcher = AFK_END_PATTERN.matcher(message);
        if (afkEndMatcher.find()) {
            storage.setAfk(false);
            return true;
        }

        Matcher deadMatcher = DEAD_PATTERN.matcher(message);
        if (deadMatcher.find()) {
            int minutes = parseInt(deadMatcher.group("minutes"));
            storage.getCountdowns().add(new Countdown("Friedhof", ofMinutes(minutes)));
            return true;
        }

        Matcher jailMatcher = JAIL_PATTERN.matcher(message);
        if (jailMatcher.find()) {
            int minutes = parseInt(jailMatcher.group("minutes"));
            storage.getCountdowns().add(new Countdown("Gefängnis", ofMinutes(minutes)));
            return true;
        }

        return true;
    }

    @Override
    public void onTick() {
        if (player.age % 1200 == 0 && !storage.isAfk()) {
            configuration.addMinutesSinceLastPayDay(1);
        }
    }
}
