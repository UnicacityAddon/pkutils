package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static java.net.URI.create;

public record BlacklistReasonDataGetRequest() implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create("https://gist.githubusercontent.com/rettichlp/54e97f4dbb3988bf22554c01d62af666/raw/pkutils-blacklistreasons.json");
    }
}
