package de.rettichlp.pkutils.common.api;

import lombok.Builder;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Builder
public class Request<T extends IRequest> {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String SESSION_TOKEN = MinecraftClient.getInstance().getSession().getAccessToken();

    private final String method;
    private final T requestData;
    @Builder.Default
    private final Map<String, String> headers = new HashMap<>();

    public CompletableFuture<HttpResponse<String>> send() {
        return supplyAsync(() -> {
            try {
                HttpRequest httpRequest = getHttpRequest();
                HttpResponse<String> httpResponse = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("Sent request: {} -> [{}] {}", httpRequest, httpResponse.statusCode(), httpResponse.body());
                return httpResponse;
            } catch (IOException | InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    private HttpRequest getHttpRequest() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.requestData.getUrl())
                .header("Content-Type", "application/json")
                .header("X-Minecraft-Session-Token", SESSION_TOKEN);

        this.headers.forEach(builder::header);

        return builder
                .method(this.method, HttpRequest.BodyPublishers.ofString(getJsonBody()))
                .build();
    }

    private String getJsonBody() {
        return api.getGson().toJson(this.requestData);
    }
}
