package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import de.rettichlp.pkutils.common.models.Faction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static java.net.URI.create;

public record FactionMemberDataGetRequest(Faction faction) implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create("https://pkaddon.de/factions/" + this.faction.getApiName());
    }
}
