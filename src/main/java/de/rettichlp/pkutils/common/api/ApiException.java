package de.rettichlp.pkutils.common.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.notificationService;

@Getter
public class ApiException extends RuntimeException {

    private final HttpResponse<String> response;

    public ApiException(@NotNull HttpResponse<String> response) {
        super("Error while sending request: [" + response.statusCode() + "] -> " + (response.body().isBlank() ? "No response body" : response.body()) + " (" + response.request().uri().toString() + ")");
        this.response = response;
    }

    public ApiException(@NotNull HttpResponse<String> response, String message) {
        super(message);
        this.response = response;
    }

    public void sendNotification() {
        notificationService.sendErrorNotification("Fehler bei der Abfrage (" + this.response.statusCode() + ")");
    }

    public void log() {
        LOGGER.warn(getMessage());
    }
}
