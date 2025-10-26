package de.rettichlp.pkutils.common.api.response;

public record ErrorResponse(int httpStatusCode, String httpStatus, String info) {
}
