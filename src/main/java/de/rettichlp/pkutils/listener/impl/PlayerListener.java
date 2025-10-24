package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.ShutdownReason.CEMETERY;
import static de.rettichlp.pkutils.common.models.ShutdownReason.JAIL;
import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static java.time.Duration.ofMinutes;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.RED;

@PKUtilsListener
public class PlayerListener implements IAbsorptionGetListener, IMessageReceiveListener, ITickListener {

    private static final String SHUTDOWN_TIMEOUT = "5";

    // afk
    private static final Pattern AFK_START_PATTERN = compile("^Du bist nun im AFK-Modus\\.$");
    private static final Pattern AFK_END_PATTERN = compile("^Du bist nun nicht mehr im AFK-Modus\\.$");

    // dead
    private static final Pattern DEAD_PATTERN = compile("^Du bist nun für (?<minutes>\\d+) Minuten auf dem Friedhof$");
    private static final Pattern DEAD_DESPAWN_PATTERN = compile("^Verdammt\\.{3} mein Kopf dröhnt so\\.{3}$");

    // jail
    private static final Pattern JAIL_PATTERN = compile("^\\[Gefängnis] Du bist nun für (?<minutes>\\d+) Minuten im Gefängnis\\.$");
    private static final Pattern JAIL_UNJAIL_PATTERN = compile("^\\[Gefängnis] Du bist nun wieder frei!$");

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

        Matcher deadDespawnMatcher = DEAD_DESPAWN_PATTERN.matcher(message);
        if (deadDespawnMatcher.find()) {
            boolean shutdown = storage.getActiveShutdowns().removeIf(shutdownReason -> shutdownReason == CEMETERY);

            if (shutdown) {
                shutdownPC();
            }

            return true;
        }

        Matcher jailUnjailMatcher = JAIL_UNJAIL_PATTERN.matcher(message);
        if (jailUnjailMatcher.find()) {
            boolean shutdown = storage.getActiveShutdowns().removeIf(shutdownReason -> shutdownReason == JAIL);

            if (shutdown) {
                shutdownPC();
            }

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

    private void shutdownPC() {
        String os = getProperty("os.name").toLowerCase();
        String[] command = new String[0];

        if (os.contains("windows")) {
            command = new String[]{ "shutdown", "/s", "/t", SHUTDOWN_TIMEOUT };
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command = new String[]{ "shutdown", "-h", "+" + SHUTDOWN_TIMEOUT }; // will fail potentially without sudo
        }

        ClientConnection connection = player.networkHandler.getConnection();
        if (nonNull(connection)) {
            connection.disconnect(empty()
                    .append(of("Der PC wird in").copy().formatted(GRAY)).append(" ")
                    .append(of(SHUTDOWN_TIMEOUT + " Sekunden").copy().formatted(RED)).append(" ")
                    .append(of("durch PKUtils heruntergefahren...").copy().formatted(GRAY)));
        }

        if (command.length == 0) {
            LOGGER.warn("Unknown operating system {} - shutdown aborted", os);
            return;
        }

        try {
            getRuntime().exec(command);
        } catch (IOException e) {
            LOGGER.error("Error while executing shutdown command: {}", command, e);
        }
    }
}
