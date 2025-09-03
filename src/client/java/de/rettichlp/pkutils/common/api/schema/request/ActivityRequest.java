package de.rettichlp.pkutils.common.api.schema.request;

import de.rettichlp.pkutils.common.api.schema.ActivityType;

import java.net.URI;

import static java.net.URI.create;

public record ActivityRequest(ActivityType activity) implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://pkutils.rettichlp.de/v1/activity/add");
    }
}
