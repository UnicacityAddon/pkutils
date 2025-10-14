package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.*;
import static de.rettichlp.pkutils.common.models.ActivityEntry.Type.EMERGENCY_SERVICE;
import static de.rettichlp.pkutils.common.models.Sound.SERVICE;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class ServiceListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern SERVICE_PATTERN = compile("Ein Notruf von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) \\((?<message>.+)\\)\\.");
    private static final Pattern SERVICE_DONE_PATTERN = compile("^Du hast den Service von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) als 'Erledigt' markiert\\.$");
    private static final Pattern SERVICE_ACCEPTED_PATTERN = compile("^HQ: (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat den Notruf von (?:\\[PK])?(?<senderName>[a-zA-Z0-9_]+) angenommen\\.$");
    private static final Pattern SERVICE_REACHED_PATTERN = compile("^Du hast dein Ziel erreicht!$");

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher serviceMatcher = SERVICE_PATTERN.matcher(message);
        if (serviceMatcher.find()) {
            SERVICE.play();
            return true;
        }

        Matcher serviceAcceptedMatcher = SERVICE_ACCEPTED_PATTERN.matcher(message);
        if (serviceAcceptedMatcher.find()) {
            String playerName = serviceAcceptedMatcher.group("playerName");
            if (playerName.equalsIgnoreCase(String.valueOf(player.getDisplayName()))) {
                storage.setActiveService(true);
            }
            return true;
        }

        Matcher serviceReachedMatcher = SERVICE_REACHED_PATTERN.matcher(message);
        if (serviceReachedMatcher.find()) {
            if (storage.isActiveService()) {
                sendCommand("/doneservice");
            }
            return true;
        }

        Matcher serviceDoneMatcher = SERVICE_DONE_PATTERN.matcher(message);
        if (serviceDoneMatcher.find()) {
            api.trackActivity(EMERGENCY_SERVICE);
            storage.setActiveService(false);
            return true;
        }

        return true;
    }
}
