package de.rettichlp.pkutils.common.api.schema.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.net.URI.create;

@Getter
@Builder
@AllArgsConstructor
public class FactionSyncRequest implements IRequest {

    private final String syncedBy;
    private final List<Map<String, Object>> members;

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/syncfactions");
    }

    @Override
    public Map<String, String> getHeaders() {
        return Map.of("X-Api-Key", "DeinGeheimerSyncKey_a9z7b3fG");
    }
}