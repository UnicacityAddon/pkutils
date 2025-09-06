package de.rettichlp.pkutils.common.services;

import com.google.gson.Gson;
import de.rettichlp.pkutils.common.api.schema.ActivityType;
import de.rettichlp.pkutils.common.api.schema.Response;
import de.rettichlp.pkutils.common.api.schema.request.ActivityClearRequest;
import de.rettichlp.pkutils.common.api.schema.request.ActivityRequest;
import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static java.util.Objects.isNull;

public class ActivityService extends PKUtilsBase {

    private static final Gson GSON = new Gson();

    public void trackActivity(ActivityType activityType) {
        MinecraftClient client = MinecraftClient.getInstance();

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (isNull(networkHandler)) {
            LOGGER.warn("Tried to track activity, but no server info found");
            return;
        }

        // KORREKTUR: Die IP-Überprüfung wurde entfernt.

        Request<ActivityRequest> request = Request.<ActivityRequest>builder()
                .body(new ActivityRequest(activityType))
                .build();

        request.send(
                response -> sendModMessage(activityType.getSuccessMessage(), true),
                throwable -> sendModMessage("Fehler beim Tracken der Aktivität!", true)
        );
    }

    public void clearActivity(String targetName) {
        Request<ActivityClearRequest> request = Request.<ActivityClearRequest>builder()
                .body(new ActivityClearRequest(targetName))
                .build();

        request.send(
                response -> {
                    if (response.statusCode() == 200) {
                        Response apiResponse = GSON.fromJson(response.body(), Response.class);
                        sendModMessage(apiResponse.getMessage(), true);
                    } else {
                        sendModMessage("Fehler beim Zurücksetzen der Aktivität!", true);
                    }
                },
                throwable -> sendModMessage("Fehler beim zurücksetzen der Aktivität!", true)
        );
    }
}