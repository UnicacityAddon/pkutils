package de.rettichlp.pkutils.common.api.schema.request;

import java.net.URI;

import static java.net.URI.create;

public record UserRegisterRequest() implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://pkutils.rettichlp.de/v1/user/register");
    }
}
