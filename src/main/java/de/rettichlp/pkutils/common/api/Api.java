package de.rettichlp.pkutils.common.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import de.rettichlp.pkutils.common.api.response.ErrorResponse;
import de.rettichlp.pkutils.common.api.response.GetUserInfoResponse;
import de.rettichlp.pkutils.common.models.ActivityEntry;
import de.rettichlp.pkutils.common.models.BlacklistReason;
import de.rettichlp.pkutils.common.models.EquipEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionEntry;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static java.lang.String.valueOf;
import static java.net.URI.create;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.Optional.ofNullable;

public class Api {

    private static final String SESSION_TOKEN = ofNullable(MinecraftClient.getInstance())
            .map(MinecraftClient::getSession)
            .map(Session::getAccessToken)
            .orElse("");

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final String baseUrl = "https://pkutils.rettichlp.de/v1"; //http://localhost:6010/pkutils/v1
    private final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("X-Minecraft-Session-Token", SESSION_TOKEN)
            .header("X-PKU-Version", valueOf(utilService.getVersion()));

    @Getter
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.parse(json.getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .create();

    public void postUserRegister() {
        post("/user/register", new Object(), () -> LOGGER.info("Successfully registered user"));
    }

    public void getUserInfo(String playerName, Consumer<GetUserInfoResponse> callback) {
        get("/user/info?playerName=" + playerName, new TypeToken<>() {}, callback);
    }

    public void getActivity(Instant from, Instant to, Consumer<List<ActivityEntry>> callback) {
        get("/activity?from=" + from + "&to=" + to, new TypeToken<>() {}, callback);
    }

    public void getActivityPlayer(Instant from, Instant to, String playerName, Consumer<List<ActivityEntry>> callback) {
        get("/activity/" + playerName + "?from=" + from + "&to=" + to, new TypeToken<>() {}, callback);
    }

    public void postActivityAdd(ActivityEntry.Type activityType) {
        if (!storage.isPunicaKitty()) {
            return;
        }

        post("/activity/add", Map.of("activityType", activityType), () -> notificationService.sendInfoNotification(activityType.getSuccessMessage()));
    }

    public void getEquip(Instant from, Instant to, Consumer<List<EquipEntry>> callback) {
        get("/equip?from=" + from + "&to=" + to, new TypeToken<>() {}, callback);
    }

    public void getEquipPlayer(Instant from, Instant to, String playerName, Consumer<List<EquipEntry>> callback) {
        get("/equip/" + playerName + "?from=" + from + "&to=" + to, new TypeToken<>() {}, callback);
    }

    public void postEquipAdd(EquipEntry.Type equipType) {
        if (!storage.isPunicaKitty()) {
            return;
        }

        post("/equip/add", Map.of("equipType", equipType), () -> notificationService.sendInfoNotification(equipType.getSuccessMessage()));
    }

    public void getFactions(Consumer<List<FactionEntry>> callback) {
        get("/factions", new TypeToken<>() {}, callback);
    }

    public void postFactions() {
        post("/factions", storage.getFactionEntries(), () -> {});
    }

    public void getMinusPoints(Consumer<Integer> callback) {
        get("/police/minuspoints", new TypeToken<>() {}, callback);
    }

    public void getMinusPointsPlayer(String playerName, Consumer<Integer> callback) {
        get("/police/minuspoints/" + playerName, new TypeToken<>() {}, callback);
    }

    public void postMinusPointsModify(String playerName, int amount) {
        if (!storage.isPunicaKitty()) {
            return;
        }

        post("/police/minuspoints/" + playerName + "/modify?amount=" + amount, amount, () -> notificationService.sendInfoNotification("Punkte erfolgreich angepasst"));
    }

    public void getBlacklistReasonData(Consumer<Map<Faction, List<BlacklistReason>>> callback) {
        get("https://gist.githubusercontent.com/rettichlp/54e97f4dbb3988bf22554c01d62af666/raw/pkutils-blacklistreasons.json", new TypeToken<>() {}, callback);
    }

    public void getModrinthVersions(Consumer<List<Map<String, Object>>> callback) {
        get("https://api.modrinth.com/v2/project/pkutils/version", new TypeToken<>() {}, callback);
    }

    private <T> void get(@NotNull String uri, TypeToken<T> typeToken, Consumer<T> callback) {
        HttpRequest httpRequest = this.requestBuilder.copy()
                .uri(uri.startsWith("https://") ? create(uri) : create(this.baseUrl + uri))
                .GET()
                .build();

        sendRequest(httpRequest, typeToken, callback);
    }

    private void post(String uri, Object bodyObject, Runnable runnable) {
        String jsonString = this.gson.toJson(bodyObject);
        HttpRequest.BodyPublisher body = ofString(jsonString);

        HttpRequest httpRequest = this.requestBuilder.copy()
                .uri(create(this.baseUrl + uri))
                .POST(body)
                .build();

        sendRequest(httpRequest, null, object -> runnable.run());
    }

    private <T> void sendRequest(HttpRequest httpRequest, TypeToken<T> typeToken, Consumer<T> callback) {
        this.httpClient.sendAsync(httpRequest, ofString())
                .thenApply(this::catchDefaultApiError)
                .thenApply(response -> ofNullable(typeToken)
                        .map(tt -> this.gson.fromJson(response.body(), tt))
                        .orElse(null))
                .thenAccept(responseObject -> {
                    LOGGER.info("Successfully sent request: [{}] {}", httpRequest.method(), httpRequest.uri().toString());
                    callback.accept(responseObject);
                })
                .exceptionally(throwable -> {
                    handleError(httpRequest, throwable);
                    return null;
                });
    }

    @Contract("_ -> param1")
    private @NotNull HttpResponse<String> catchDefaultApiError(@NotNull HttpResponse<String> response) {
        // check if the status code is 2xx and throw an exception if not
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            ErrorResponse errorResponse = getGson().fromJson(response.body(), ErrorResponse.class);
            throw new PKUtilsApiException(errorResponse);
        }

        return response;
    }

    private void handleError(HttpRequest httpRequest, @NotNull Throwable throwable) {
        if (throwable.getCause() instanceof PKUtilsApiException pkUtilsApiException) {
            pkUtilsApiException.sendNotification();
            pkUtilsApiException.getErrorResponse().log();
        } else {
            LOGGER.warn("Error while sending request: [{}] {} ({})", httpRequest.method(), httpRequest.uri().toString(), throwable.getMessage());
        }
    }
}
