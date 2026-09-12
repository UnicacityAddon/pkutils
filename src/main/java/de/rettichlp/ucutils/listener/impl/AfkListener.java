package de.rettichlp.ucutils.listener.impl;

import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static java.time.Duration.between;
import static java.time.LocalDate.now;
import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.regex.Pattern.compile;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class AfkListener implements IMessageReceiveListener {

    private static final Pattern AFK_MODE_PATTERN = compile("^Name: (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) \\| AFK-Modus seit (?<time>\\d{2}:\\d{2})$");

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher afkModeMatcher = AFK_MODE_PATTERN.matcher(message);
        if (afkModeMatcher.find()) {
            String playerName = afkModeMatcher.group("playerName");
            String timeString = afkModeMatcher.group("time");
            LocalTime time = parse(timeString, ofPattern("HH:mm"));

            LocalDateTime afkStart = LocalDateTime.of(now(utilService.getServerZoneId()), time);
            LocalDateTime now = LocalDateTime.now(utilService.getServerZoneId());
            if (afkStart.isAfter(now)) {
                afkStart = afkStart.minusDays(1);
            }

            long minutesSinceAfk = between(afkStart, now).toMinutes();
            long hours = minutesSinceAfk / 60;
            long minutes = minutesSinceAfk % 60;

            String hoursString = hours + " " + (hours == 1 ? "Stunde" : "Stunden");
            String minutesString = minutes + " " + (minutes == 1 ? "Minute" : "Minuten");
            String durationString = (hours > 0 ? hoursString + " " : "") + minutesString;

            MutableComponent modifiedText = empty()
                    .append(literal("Name: " + playerName + " | AFK-Modus seit " + timeString).withColor(GRAY))
                    .append(SPACE)
                    .append(literal("(").withColor(DARK_GRAY))
                    .append(literal(durationString).withColor(RED))
                    .append(literal(")").withColor(DARK_GRAY));

            player.sendSystemMessage(modifiedText);
            return false;
        }

        return true;
    }
}
