package de.rettichlp.pkutils.common.api.schema.request;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public interface IRequest {

    URI getUrl();

    default Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }
}