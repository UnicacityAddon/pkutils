package de.rettichlp.pkutils.common.api;

import de.rettichlp.pkutils.common.api.response.ErrorResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;

@Getter
public class PKUtilsApiException extends ApiException {

    public PKUtilsApiException(@NotNull HttpResponse<String> response, @NotNull ErrorResponse errorResponse) {
        super(response, "Error while sending PKUtils API request: [" + errorResponse.httpStatusCode() + "] " + errorResponse.httpStatus() + " -> " + errorResponse.info() + " (" + response.request().uri().toString() + ")");
    }
}
