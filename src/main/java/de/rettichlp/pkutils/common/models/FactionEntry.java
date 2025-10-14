package de.rettichlp.pkutils.common.models;

import java.util.Set;

public record FactionEntry(Faction faction, Set<FactionMember> members) {

}
