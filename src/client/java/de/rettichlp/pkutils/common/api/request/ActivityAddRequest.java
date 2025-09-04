package de.rettichlp.pkutils.common.api.schema.request;

import de.rettichlp.pkutils.common.api.schema.ActivityType;
import de.rettichlp.pkutils.common.api.IRequest;

import java.net.URI;

import static java.net.URI.create;

public record ActivityRequest(ActivityType activityType) implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://pkutils.rettichlp.de/v1/activity/add");
    }
}
