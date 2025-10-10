package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Instant;

import static de.rettichlp.pkutils.PKUtils.api;
import static java.net.URI.create;

public record ActivityGetRequest(Instant from, Instant to) implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create(api.getBaseUrl() + "/activity?from=" + this.from.toString() + "&to=" + this.to.toString());
    }
}
