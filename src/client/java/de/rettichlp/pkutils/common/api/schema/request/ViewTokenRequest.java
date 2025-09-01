package de.rettichlp.pkutils.common.api.schema.request;

import java.net.URI;

import static java.net.URI.create;

public record ViewTokenRequest(String targetPlayer) implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/requesttoken");
    }
}
