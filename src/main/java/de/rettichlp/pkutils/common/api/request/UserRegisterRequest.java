package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;

import java.net.URI;

import static de.rettichlp.pkutils.PKUtils.api;
import static java.net.URI.create;

public record UserRegisterRequest() implements IRequest {

    @Override
    public URI getUrl() {
        return create(api.getBaseUrl() + "/user/register");
    }
}
