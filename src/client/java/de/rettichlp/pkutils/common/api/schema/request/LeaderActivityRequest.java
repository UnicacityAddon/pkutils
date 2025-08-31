package de.rettichlp.pkutils.common.api.schema.request;

import lombok.NoArgsConstructor;
import java.net.URI;
import static java.net.URI.create;

@NoArgsConstructor
public class LeaderActivityRequest implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/requestleadertoken");
    }
}
