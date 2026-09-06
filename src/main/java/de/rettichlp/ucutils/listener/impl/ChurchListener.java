package de.rettichlp.ucutils.listener.impl;

import de.rettichlp.ucutils.common.models.Countdown;
import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

@UCUtilsListener
public class PrayListener implements IMessageReceiveListener {

    private static final Pattern PRAYING_START_PATTERN = compile("^\\[Kirche] Du hast begonnen für (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) zu beten\\.$");
    private static final Pattern BLESSING_GIVEN_PATTERN = compile("^\\[Segen] Du hast (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) gesegnet\\.$");

    private static final Duration BLESSING_DURATION = ofMinutes(2);

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher prayingStartMatcher = PRAYING_START_PATTERN.matcher(message);
        if (prayingStartMatcher.find()) {
            String playerName = prayingStartMatcher.group("playerName");
            utilService.delayedAction(() -> commandService.sendCommandWithAfkCheck("beten " + playerName), 15000);
            return true;
        }

        Matcher blessingGivenMatcher = BLESSING_GIVEN_PATTERN.matcher(message);
        if (blessingGivenMatcher.find()) {
            new Countdown("Segen", BLESSING_DURATION);
            return true;
        }

        return true;
    }
}
