package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.activityService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.common.api.schema.ActivityType.EMERGENCY_SERVICE;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class ServiceListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern SERVICE_ACCEPT_PATTERN = compile("^HQ: (?<playerName>[a-zA-Z0-9_]+) hat den Notruf von (?<targetName>[a-zA-Z0-9_]+) angenommen\\.$");

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher serviceAcceptMatcher = SERVICE_ACCEPT_PATTERN.matcher(message);
        if (serviceAcceptMatcher.find()) {
            String playerName = serviceAcceptMatcher.group("playerName");
            if (player.getName().getString().equals(playerName)) {
                activityService.trackActivity(EMERGENCY_SERVICE);
            }
        }

        return true;
    }
}
