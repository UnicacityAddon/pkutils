package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.ActivityEntry.Type.EMERGENCY_SERVICE;
import static de.rettichlp.pkutils.common.models.Sound.SERVICE;
import static java.lang.Math.max;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class EmergencyServiceListener extends PKUtilsBase implements IMessageReceiveListener, INaviSpotReachedListener {

    private static final Pattern SERVICE_PATTERN = compile("Ein Notruf von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) \\((?<message>.+)\\)\\.");
    private static final Pattern SERVICE_ACCEPTED_PATTERN = compile("^(?:HQ: )?(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat den Notruf von (?:\\[PK])?(?<senderName>[a-zA-Z0-9_]+) angenommen\\.$");
    private static final Pattern SERVICE_REQUEUED_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat den Notruf von (?:\\[PK])?(?<senderName>[a-zA-Z0-9_]+) erneut geöffnet\\.$");
    private static final Pattern SERVICE_DONE_PATTERN = compile("^Du hast den Service von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) als 'Erledigt' markiert\\.$");

    private boolean activeService = false;

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher serviceMatcher = SERVICE_PATTERN.matcher(message);
        if (serviceMatcher.find()) {
            storage.setActiveServices(storage.getActiveServices() + 1);
            SERVICE.play();
            return true;
        }

        Matcher serviceAcceptedMatcher = SERVICE_ACCEPTED_PATTERN.matcher(message);
        if (serviceAcceptedMatcher.find()) {
            String playerName = serviceAcceptedMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().getName())) {
                this.activeService = true;
                storage.setActiveServices(max(0, storage.getActiveServices() - 1));
            }

            return true;
        }

        Matcher serviceRequeuedMatcher = SERVICE_REQUEUED_PATTERN.matcher(message);
        if (serviceRequeuedMatcher.find()) {
            String playerName = serviceRequeuedMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().getName())) {
                this.activeService = false;
                storage.setActiveServices(storage.getActiveServices() + 1);
            }

            return true;
        }

        Matcher serviceDoneMatcher = SERVICE_DONE_PATTERN.matcher(message);
        if (serviceDoneMatcher.find()) {
            api.postActivityAdd(EMERGENCY_SERVICE);
            this.activeService = false;
            return true;
        }

        return true;
    }

    @Override
    public void onNaviSpotReached() {
        if (this.activeService) {
            sendCommand("/doneservice");
        }
    }
}
