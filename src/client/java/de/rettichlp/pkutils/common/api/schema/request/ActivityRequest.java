package de.rettichlp.pkutils.common.api.schema.request;

import de.rettichlp.pkutils.common.api.schema.ActivityType;
import lombok.Data;

import java.net.URI;

@Data
public class ActivityRequest implements IRequest {

    private final ActivityType activity;

    @Override
    public URI getUrl() {
        return URI.create("https://activitycheck.pkutils.eu/proxy");
    }
}
