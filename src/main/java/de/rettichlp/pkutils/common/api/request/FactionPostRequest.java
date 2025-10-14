package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import de.rettichlp.pkutils.common.models.FactionEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.api;
import static java.net.URI.create;

public record FactionPostRequest(Set<FactionEntry> factionEntries) implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create(api.getBaseUrl() + "/factions");
    }
}
