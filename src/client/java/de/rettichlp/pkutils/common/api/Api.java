package de.rettichlp.pkutils.common.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import de.rettichlp.pkutils.common.api.request.ActivityAddRequest;
import de.rettichlp.pkutils.common.api.request.ActivityGetPlayerRequest;
import de.rettichlp.pkutils.common.api.request.ActivityGetRequest;
import de.rettichlp.pkutils.common.api.request.RegisterPlayerRequest;
import de.rettichlp.pkutils.common.api.schema.Activity;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.gson.reflect.TypeToken.getParameterized;
import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Objects.isNull;

@SuppressWarnings("unchecked")
public class Api {

    @Getter
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.parse(json.getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .create();

    public void registerPlayer() {
        Request<RegisterPlayerRequest> request = Request.<RegisterPlayerRequest>builder()
                .method("POST")
                .requestData(new RegisterPlayerRequest(storage.getFactionMembers()))
                .build();

        request.send().thenAccept(httpResponse -> {
            validate(httpResponse);
            hudService.sendSuccessNotification("API Login erfolgreich");
        }).exceptionally(throwable -> {
            LOGGER.error("Error while registering player", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    public CompletableFuture<List<Activity>> getActivities(Instant from, Instant to) {
        Request<ActivityGetRequest> request = Request.<ActivityGetRequest>builder()
                .method("GET")
                .requestData(new ActivityGetRequest(from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, Activity.class).getType();
            return (List<Activity>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activities", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public CompletableFuture<List<Activity>> getActivitiesForPlayer(String playerName, Instant from, Instant to) {
        Request<ActivityGetPlayerRequest> request = Request.<ActivityGetPlayerRequest>builder()
                .method("GET")
                .requestData(new ActivityGetPlayerRequest(playerName, from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, Activity.class).getType();
            return (List<Activity>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activities for player {}", playerName, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public void trackActivity(Activity.Type activityType) {
        MinecraftClient client = MinecraftClient.getInstance();

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (isNull(networkHandler)) {
            LOGGER.warn("Tried to track activity, but no server info found");
            return;
        }

        String addressString = networkHandler.getConnection().getAddress().toString(); // punicakitty.de/152.53.252.60:25565
        if (!addressString.contains("152.53.252.60")) {
            LOGGER.warn("Tried to track activity, but not on supported server");
            return;
        }

        Request<ActivityAddRequest> request = Request.<ActivityAddRequest>builder()
                .method("POST")
                .requestData(new ActivityAddRequest(activityType))
                .build();

        request.send().thenAccept(httpResponse -> {
            validate(httpResponse);
            hudService.sendInfoNotification(activityType.getSuccessMessage());
        }).exceptionally(throwable -> {
            LOGGER.error("Error while tracking activity {}", activityType, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    private void validate(@NotNull HttpResponse<String> httpResponse) {
        int statusCode = httpResponse.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            ErrorResponse errorResponse = this.gson.fromJson(httpResponse.body(), ErrorResponse.class);
            throw new PKUtilsApiException(errorResponse);
        }
    }

    private <T> T validateAndParse(@NotNull HttpResponse<String> httpResponse, Type type) {
        validate(httpResponse);
        return this.gson.fromJson(httpResponse.body(), type);
    }

    public record ErrorResponse(int httpStatusCode, String httpStatus, String info) {

    }
}
