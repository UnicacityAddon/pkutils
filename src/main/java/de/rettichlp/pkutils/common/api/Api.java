package de.rettichlp.pkutils.common.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import de.rettichlp.pkutils.common.api.request.ActivityAddRequest;
import de.rettichlp.pkutils.common.api.request.ActivityGetPlayerRequest;
import de.rettichlp.pkutils.common.api.request.ActivityGetRequest;
import de.rettichlp.pkutils.common.api.request.BlacklistReasonDataGetRequest;
import de.rettichlp.pkutils.common.api.request.EquipAddRequest;
import de.rettichlp.pkutils.common.api.request.EquipGetPlayerRequest;
import de.rettichlp.pkutils.common.api.request.EquipGetRequest;
import de.rettichlp.pkutils.common.api.request.FactionMemberDataGetRequest;
import de.rettichlp.pkutils.common.api.request.PoliceMinusPointsGetPlayerRequest;
import de.rettichlp.pkutils.common.api.request.PoliceMinusPointsGetRequest;
import de.rettichlp.pkutils.common.api.request.PoliceMinusPointsModifyRequest;
import de.rettichlp.pkutils.common.api.request.UserInfoRequest;
import de.rettichlp.pkutils.common.api.request.UserRegisterRequest;
import de.rettichlp.pkutils.common.models.ActivityEntry;
import de.rettichlp.pkutils.common.models.BlacklistReason;
import de.rettichlp.pkutils.common.models.EquipEntry;
import de.rettichlp.pkutils.common.models.Faction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.gson.reflect.TypeToken.getParameterized;
import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.lang.Integer.MIN_VALUE;

@SuppressWarnings("unchecked")
public class Api {

    @Getter
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.parse(json.getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .create();

    @Getter
    private final String baseUrl = "https://pkutils.rettichlp.de/v1"; // http://localhost:6010/pkutils/v1

    public CompletableFuture<Void> registerUser(String version) {
        Request<UserRegisterRequest> request = Request.<UserRegisterRequest>builder()
                .method("POST")
                .requestData(new UserRegisterRequest(storage.getFactionMembers()))
                .headers(Map.of("X-PKU-Version", version))
                .build();

        return request.send().thenAccept(httpResponse -> {
            validate(httpResponse);
            LOGGER.info("User successfully registered on PKUtils API");
        }).exceptionally(throwable -> {
            LOGGER.error("Error while registering user", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    public CompletableFuture<Map<String, Object>> getUserInfo(String playerName) {
        Request<UserInfoRequest> request = Request.<UserInfoRequest>builder()
                .method("GET")
                .requestData(new UserInfoRequest(playerName))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(Map.class, String.class, Object.class).getType();
            return (Map<String, Object>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching user info", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new HashMap<>();
        });
    }

    public CompletableFuture<List<ActivityEntry>> getActivityEntries(Instant from, Instant to) {
        Request<ActivityGetRequest> request = Request.<ActivityGetRequest>builder()
                .method("GET")
                .requestData(new ActivityGetRequest(from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, ActivityEntry.class).getType();
            return (List<ActivityEntry>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activity entries", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public CompletableFuture<List<ActivityEntry>> getActivityEntriesForPlayer(String playerName, Instant from, Instant to) {
        Request<ActivityGetPlayerRequest> request = Request.<ActivityGetPlayerRequest>builder()
                .method("GET")
                .requestData(new ActivityGetPlayerRequest(playerName, from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, ActivityEntry.class).getType();
            return (List<ActivityEntry>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activity entries for player {}", playerName, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public void trackActivity(ActivityEntry.Type activityType) {
        if (!storage.isPunicaKitty()) {
            return;
        }

        Request<ActivityAddRequest> request = Request.<ActivityAddRequest>builder()
                .method("POST")
                .requestData(new ActivityAddRequest(activityType))
                .build();

        request.send().thenAccept(httpResponse -> {
            validate(httpResponse);
            notificationService.sendInfoNotification(activityType.getSuccessMessage());
        }).exceptionally(throwable -> {
            LOGGER.error("Error while tracking activity {}", activityType, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    public CompletableFuture<List<EquipEntry>> getEquipEntries(Instant from, Instant to) {
        Request<EquipGetRequest> request = Request.<EquipGetRequest>builder()
                .method("GET")
                .requestData(new EquipGetRequest(from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, EquipEntry.class).getType();
            return (List<EquipEntry>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching equip entries", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public CompletableFuture<List<EquipEntry>> getEquipEntriesForPlayer(String playerName, Instant from, Instant to) {
        Request<EquipGetPlayerRequest> request = Request.<EquipGetPlayerRequest>builder()
                .method("GET")
                .requestData(new EquipGetPlayerRequest(playerName, from, to))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(List.class, EquipEntry.class).getType();
            return (List<EquipEntry>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching equip entries for player {}", playerName, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new ArrayList<>();
        });
    }

    public void trackEquip(EquipEntry.Type equipType) {
        Request<EquipAddRequest> request = Request.<EquipAddRequest>builder()
                .method("POST")
                .requestData(new EquipAddRequest(equipType))
                .build();

        request.send().thenAccept(httpResponse -> {
            validate(httpResponse);
            notificationService.sendInfoNotification(equipType.getSuccessMessage());
        }).exceptionally(throwable -> {
            LOGGER.error("Error while tracking equip {}", equipType, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    public CompletableFuture<Integer> getMinusPoints() {
        Request<PoliceMinusPointsGetRequest> request = Request.<PoliceMinusPointsGetRequest>builder()
                .method("GET")
                .requestData(new PoliceMinusPointsGetRequest())
                .build();

        return request.send().thenApply(httpResponse -> (Integer) validateAndParse(httpResponse, Integer.class)).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activities", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return MIN_VALUE;
        });
    }

    public CompletableFuture<Integer> getMinusPointsForPlayer(String playerName) {
        Request<PoliceMinusPointsGetPlayerRequest> request = Request.<PoliceMinusPointsGetPlayerRequest>builder()
                .method("GET")
                .requestData(new PoliceMinusPointsGetPlayerRequest(playerName))
                .build();

        return request.send().thenApply(httpResponse -> (Integer) validateAndParse(httpResponse, Integer.class)).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activities for player {}", playerName, throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return MIN_VALUE;
        });
    }

    public CompletableFuture<Integer> modifyMinusPoints(String playerName, int amount) {
        Request<PoliceMinusPointsModifyRequest> request = Request.<PoliceMinusPointsModifyRequest>builder()
                .method("POST")
                .requestData(new PoliceMinusPointsModifyRequest(playerName, amount))
                .build();

        return request.send().thenApply(httpResponse -> (Integer) validateAndParse(httpResponse, Integer.class)).exceptionally(throwable -> {
            LOGGER.error("Error while fetching activities", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return MIN_VALUE;
        });
    }

    public CompletableFuture<Map<Faction, List<BlacklistReason>>> getBlacklistReasonData() {
        Request<BlacklistReasonDataGetRequest> request = Request.<BlacklistReasonDataGetRequest>builder()
                .method("GET")
                .requestData(new BlacklistReasonDataGetRequest())
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = new TypeToken<Map<Faction, List<BlacklistReason>>>() {}.getType();
            return (Map<Faction, List<BlacklistReason>>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching faction data", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return null;
        });
    }

    public CompletableFuture<Map<String, Object>> getFactionMemberData(Faction faction) {
        Request<FactionMemberDataGetRequest> request = Request.<FactionMemberDataGetRequest>builder()
                .method("GET")
                .requestData(new FactionMemberDataGetRequest(faction))
                .build();

        return request.send().thenApply(httpResponse -> {
            Type type = getParameterized(Map.class, String.class, Object.class).getType();
            return (Map<String, Object>) validateAndParse(httpResponse, type);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while fetching faction data", throwable);

            if (throwable instanceof CompletionException completionException && completionException.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
                pkUtilsApiException.sendNotification();
            }

            return new HashMap<>();
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
