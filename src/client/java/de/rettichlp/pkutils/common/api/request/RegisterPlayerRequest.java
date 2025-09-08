package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtilsClient.api;
import static java.net.URI.create;

public record RegisterPlayerRequest(Map<Faction, Set<FactionMember>> factionMembers) implements IRequest {

    @Override
    public URI getUrl() {
        return create(api.getBaseUrl() + "/user/register");
    }
}
