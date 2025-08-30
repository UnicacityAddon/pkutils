package de.rettichlp.pkutils.common.api.schema.request;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.net.URI;

@Builder
@AllArgsConstructor
public class ActivityClearRequest implements IRequest {

    private final String playerName;

    @Override
    public URI getUrl() {
        return URI.create("https://activitycheck.pkutils.eu/clearactivity");
    }
}
