package de.rettichlp.pkutils.common.api.schema;

import lombok.Getter;

@Getter
public class ErrorResponse extends Response {

    private final String httpStatus;
    private final String info;
    private final int httpStatusCode;

    public ErrorResponse(String httpStatus, String info, int httpStatusCode) {
        super(info);
        this.httpStatus = httpStatus;
        this.info = info;
        this.httpStatusCode = httpStatusCode;
    }
}
