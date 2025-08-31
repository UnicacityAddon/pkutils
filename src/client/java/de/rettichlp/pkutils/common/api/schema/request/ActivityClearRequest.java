package de.rettichlp.pkutils.common.api.schema.request;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.net.URI;

import static java.net.URI.create;

@Builder
@AllArgsConstructor
public class ActivityClearRequest implements IRequest {

    private final String playerName;

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/clearactivity");
    }
}
