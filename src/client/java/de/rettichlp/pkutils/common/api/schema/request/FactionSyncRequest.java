package de.rettichlp.pkutils.common.api.schema.request;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.net.URI.create;

public record FactionSyncRequest(String syncedBy, List<Map<String, Object>> members) implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/syncfactions");
    }

    @Override
    public Map<String, String> getHeaders() {
        return Map.of("X-Api-Key", "DeinGeheimerSyncKey_a9z7b3fG");
    }
}
