package de.rettichlp.pkutils.common.api.response;

import static de.rettichlp.pkutils.PKUtils.LOGGER;

public record ErrorResponse(int httpStatusCode, String httpStatus, String info) {

    public void log() {
        LOGGER.warn("Error while sending PKUtils API request: [{}] {} -> {}", this.httpStatusCode, this.httpStatus, this.info);
    }
}
