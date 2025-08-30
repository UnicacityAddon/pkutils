package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.api.schema.ActivityType;
import de.rettichlp.pkutils.common.api.schema.request.ActivityClearRequest;
import de.rettichlp.pkutils.common.api.schema.request.ActivityRequest;
import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import static de.rettichlp.pkutils.PKUtils.LOGGER;

public class ActivityService extends PKUtilsBase {

    public void trackActivity(ActivityType activityType) {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) {
            LOGGER.warn("Tried to track activity, but no server info found");
            return;
        }

        String address = serverInfo.address;
        if (!address.equals("152.53.252.60")) {
            LOGGER.warn("Tried to track activity, but not on supported server");
            return;
        }

        Request<ActivityRequest> request = Request.<ActivityRequest>builder()
                .body(new ActivityRequest(activityType))
                .build();

        request.send(response -> sendModMessage(activityType.getSuccessMessage(), true), () -> sendModMessage("Fehler beim Tracken der Aktivität!", true));
    }

    public void clearActivity(String targetName) {
        Request<ActivityClearRequest> request = Request.<ActivityClearRequest>builder()
                .body(new ActivityClearRequest(targetName))
                .build();

        request.send(response -> sendModMessage(response.getMessage(), true), () -> sendModMessage("Fehler beim zurücksetzen der Aktivität!", true));
    }
}
