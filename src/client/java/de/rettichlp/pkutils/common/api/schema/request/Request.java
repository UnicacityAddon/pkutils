package de.rettichlp.pkutils.common.api.schema.request;

import com.google.gson.Gson;
import de.rettichlp.pkutils.common.api.schema.ErrorResponse;
import de.rettichlp.pkutils.common.api.schema.Response;
import lombok.Builder;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Builder
public class Request<T extends IRequest> {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static final String SESSION_TOKEN = MinecraftClient.getInstance().getSession().getAccessToken();

    private final T body;

    public void send(Consumer<Response> successCallback, Consumer<ErrorResponse> failureCallback) {
        supplyAsync(() -> {
            try {
                HttpRequest httpRequest = getHttpRequest();
                HttpResponse<String> httpResponse = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                    LOGGER.info("Request successful: {} -> [{}] {}", httpRequest, httpResponse.statusCode(), httpResponse.body());
                    Response response = GSON.fromJson(httpResponse.body(), Response.class);
                    successCallback.accept(response);
                } else {
                    LOGGER.warn("Request failed: {} -> [{}] {}", httpRequest, httpResponse.statusCode(), httpResponse.body());
                    ErrorResponse response = GSON.fromJson(httpResponse.body(), ErrorResponse.class);
                    failureCallback.accept(response);
                }
            } catch (IOException | InterruptedException e) {
                throw new CompletionException(e);
            }

            return null; // return nothing, everything was already handled in the callbacks
        });
    }

    private HttpRequest getHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(this.body.getUrl())
                .header("Content-Type", "application/json")
                .header("X-Minecraft-Session-Token", SESSION_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(getJsonBody()))
                .build();
    }

    private String getJsonBody() {
        return GSON.toJson(this.body);
    }
}
