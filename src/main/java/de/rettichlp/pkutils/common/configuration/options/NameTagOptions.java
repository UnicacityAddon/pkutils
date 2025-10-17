package de.rettichlp.pkutils.common.configuration.options;

import de.rettichlp.pkutils.common.models.Color;
import de.rettichlp.pkutils.common.models.Faction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(fluent = true)
public class NameTagOptions {

    private boolean factionInformation = true;
    private Map<Faction, Color> highlightFactions = new HashMap<>();
    private boolean additionalBlacklist = true;
    private boolean additionalContract = true;
    private boolean additionalHouseban = true;
    private boolean additionalWanted = true;
}
