package de.rettichlp.pkutils.common.api.schema.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.net.URI;

import static java.net.URI.create;

@Getter
@Builder
@AllArgsConstructor
public class ViewTokenRequest implements IRequest {

    private final String targetPlayer;

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/requesttoken");
    }
}