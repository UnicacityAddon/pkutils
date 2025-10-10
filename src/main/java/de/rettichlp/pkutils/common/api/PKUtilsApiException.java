package de.rettichlp.pkutils.common.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.notificationService;

@Getter
public class PKUtilsApiException extends RuntimeException {

    private final Api.ErrorResponse errorResponse;

    public PKUtilsApiException(Api.@NotNull ErrorResponse errorResponse) {
        super(errorResponse.info());
        this.errorResponse = errorResponse;
    }

    public void sendNotification() {
        notificationService.sendErrorNotification("API Fehler: [" + this.errorResponse.httpStatusCode() + "] " + this.errorResponse.info());
    }
}
