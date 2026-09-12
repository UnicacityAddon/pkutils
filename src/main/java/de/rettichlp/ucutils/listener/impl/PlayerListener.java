package de.rettichlp.ucutils.listener.impl;

import de.rettichlp.ucutils.common.models.Countdown;
import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.LOGGER;
import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.common.models.ShutdownReason.CEMETERY;
import static de.rettichlp.ucutils.common.models.ShutdownReason.JAIL;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class PlayerListener implements IMessageReceiveListener {

    private static final String SHUTDOWN_TIMEOUT = "5";

    // dead
    private static final Pattern DEAD_PATTERN = compile("^Du bist nun für (?<minutes>\\d+) Minuten auf dem Friedhof\\.$");
    private static final Pattern DEAD_DESPAWN_PATTERN = compile("^Verdammt\\.{3} mein Kopf dröhnt so\\.{3}$");
    private static final Pattern DEAD_AREVIVE_PATTERN = compile("^Du wurdest von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) wiederbelebt\\.$");

    // health
    private static final Pattern HEALTH_HEADER_PATTERN = compile("^=== Zustand von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) ===$");
    private static final Pattern HEALTH_ENTRY_PATTERN = compile("^§.» (?<type>Gesundheit|Blut §.\\[§..+§.]|Hunger|Durst|Fett|Muskeln|Sucht)§.: §.((§.)?#)+$");
    private static final Pattern HEALTH_ENTRY_HOVER_PATTERN = compile("^§.(?<value>\\d+(\\.\\d+)?)§./§.20\\.0$");
    private static final Pattern HOUSE_DRINK_PATTERN = compile("^\\[Küche] Du hast etwas getrunken\\.$");

    // jail
    private static final Pattern JAIL_PATTERN = compile("^\\[Gefängnis] Du bist nun für (?<minutes>\\d+) Minuten im Gefängnis\\.$");
    private static final Pattern JAIL_UNJAIL_PATTERN = compile("^\\[Gefängnis] Du bist nun wieder frei!$");

    // premium
    private static final Pattern PREMIUM_PATTERN = compile("^\\[Premium] Dein Premium Account ist noch .+ aktiv\\.$");

    // requests
    private static final Pattern ACCEPT_PATTERN = compile("^\\[Deal] (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat den Deal angenommen\\.$");
    private static final Pattern DECLINE_PATTERN = compile("^\\[Deal] (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat das Angebot abgelehnt\\.$");

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher deadAReviveMatcher = DEAD_AREVIVE_PATTERN.matcher(message);
        if (deadAReviveMatcher.find()) {
            storage.getActiveShutdowns().removeIf(shutdownReason -> shutdownReason == CEMETERY);
            return true;
        }

        Matcher deadMatcher = DEAD_PATTERN.matcher(message);
        if (deadMatcher.find()) {
            storage.setDead(true);
            return true;
        }

        Matcher healthHeaderMatcher = HEALTH_HEADER_PATTERN.matcher(message);
        if (healthHeaderMatcher.find()) {
            return commandService.showCommandOutputMessage("health");
        }

        Matcher healthEntryMatcher = HEALTH_ENTRY_PATTERN.matcher(message);
        if (healthEntryMatcher.find()) {
            if (!healthEntryMatcher.group("type").contains("Durst")) {
                return commandService.showCommandOutputMessage("health");
            }

            text.getSiblings().stream()
                    .map(sibling -> sibling.getStyle().getHoverEvent())
                    .filter(hoverEvent -> hoverEvent instanceof HoverEvent.ShowText)
                    .map(hoverEvent -> ((HoverEvent.ShowText) hoverEvent).value().getString())
                    .findFirst()
                    .ifPresent(hoverString -> {
                        Matcher healthEntryHoverMatcher = HEALTH_ENTRY_HOVER_PATTERN.matcher(hoverString);
                        if (healthEntryHoverMatcher.find()) {
                            storage.setHydration(parseDouble(healthEntryHoverMatcher.group("value")));
                        }
                    });

            return commandService.showCommandOutputMessage("health");
        }

        Matcher houseDrinkMatcher = HOUSE_DRINK_PATTERN.matcher(message);
        if (houseDrinkMatcher.find()) {
            commandService.sendCommandWithHiddenOutput("health");
            return commandService.showCommandOutputMessage("health");
        }

        Matcher jailMatcher = JAIL_PATTERN.matcher(message);
        if (jailMatcher.find()) {
            int minutes = parseInt(jailMatcher.group("minutes"));
            new Countdown("Gefängnis", ofMinutes(minutes));
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

        Matcher premiumMatcher = PREMIUM_PATTERN.matcher(message);
        if (premiumMatcher.find()) {
            storage.setPremium(true);
            return true;
        }

        Matcher acceptMatcher = ACCEPT_PATTERN.matcher(message);
        Matcher declineMatcher = DECLINE_PATTERN.matcher(message);
        if (acceptMatcher.find() || declineMatcher.find()) {
            List<String> requestCommands = commandService.getRequestCommands();
            if (!requestCommands.isEmpty()) {
                commandService.sendCommand(requestCommands.removeFirst());
            }
            return true;
        }

        return true;
    }

    private void shutdownPC() {
        String os = getProperty("os.name").toLowerCase();
        String[] command = new String[0];

        if (os.contains("windows")) {
            command = new String[]{ "shutdown", "/s", "/t", SHUTDOWN_TIMEOUT };
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command = new String[]{ "shutdown", "-h", "+" + SHUTDOWN_TIMEOUT }; // will fail potentially without sudo
        }

        Connection connection = player.connection.getConnection();
        connection.disconnect(empty()
                .append(literal("Der PC wird in").withColor(GRAY)).append(SPACE)
                .append(literal(SHUTDOWN_TIMEOUT + " Sekunden").withColor(RED)).append(SPACE)
                .append(literal("durch UCUtils heruntergefahren...").withColor(GRAY)));

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
