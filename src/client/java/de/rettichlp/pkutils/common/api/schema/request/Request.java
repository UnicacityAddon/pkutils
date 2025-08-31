package de.rettichlp.pkutils.common.api.schema.request;

import com.google.gson.Gson;
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

    public void send(Consumer<HttpResponse<String>> successCallback, Consumer<Throwable> failureCallback) {
        supplyAsync(() -> {
            try {
                HttpRequest httpRequest = getHttpRequest();
                HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("Sent request: {} -> [{}] {}", httpRequest.uri(), response.statusCode(), response.body());
                return response;
            } catch (IOException | InterruptedException e) {
                throw new CompletionException(e);
            }
        }).thenAccept(successCallback).exceptionally(throwable -> {
            LOGGER.error("Failed to send request", throwable);
            failureCallback.accept(throwable);
            return null;
        });
    }

    private HttpRequest getHttpRequest() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.body.getUrl())
                .header("Content-Type", "application/json")
                .header("Authorization", SESSION_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(getJsonBody()));

        this.body.getHeaders().forEach(builder::header);

        return builder.build();
    }

    private String getJsonBody() {
        return GSON.toJson(this.body);
    }
}