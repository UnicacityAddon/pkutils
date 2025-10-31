package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class EventListener implements IMessageReceiveListener {

    private static final Pattern HALLOWEEN_DOOR_VISITED_PATTERN = compile("^\\[Halloween] Du hast bei Haus (?<number>\\d+) geklingelt\\.$");

    private static final Predicate<HalloweenDoor> HALLOWEEN_DOOR_REMOVE_PREDICATE = halloweenDoor -> {
        LocalDateTime now = now();
        LocalDateTime today4am = now.withHour(4).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime timestamp = halloweenDoor.getTimestamp();
        return now.isAfter(today4am)
                ? timestamp.isBefore(today4am)
                : timestamp.isBefore(today4am.minusDays(1));
    };

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher halloweenDoorVisitedMatcher = HALLOWEEN_DOOR_VISITED_PATTERN.matcher(message);
        if (halloweenDoorVisitedMatcher.find()) {
            int doorNumber = parseInt(halloweenDoorVisitedMatcher.group("number"));
            HalloweenDoor halloweenDoor = new HalloweenDoor(doorNumber);

            Set<HalloweenDoor> halloweenClickedDoors = configuration.getHalloweenClickedDoors();
            halloweenClickedDoors.removeIf(HALLOWEEN_DOOR_REMOVE_PREDICATE);
            halloweenClickedDoors.add(halloweenDoor);
            return true;
        }

        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public static class HalloweenDoor {

        private final LocalDateTime timestamp = now();
        private final int doorNumber;
    }
}
