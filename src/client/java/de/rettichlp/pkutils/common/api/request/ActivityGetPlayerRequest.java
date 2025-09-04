package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Instant;

import static java.net.URI.create;

public record ActivityGetPlayerRequest(String minecraftName, Instant from, Instant to) implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create("https://pkutils.rettichlp.de/v1/activity/" + this.minecraftName + "?from=" + this.from.toString() + "&to=" + this.to.toString());
    }
}
