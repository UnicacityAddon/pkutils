package de.rettichlp.pkutils.common.api.schema.request;

import de.rettichlp.pkutils.common.api.IRequest;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static java.net.URI.create;

public record UserRegisterRequest(Map<Faction, Set<FactionMember>> factionMembers) implements IRequest {

    @Override
    public URI getUrl() {
        return create("https://pkutils.rettichlp.de/v1/user/register");
    }
}
