package de.rettichlp.pkutils.common.models.config;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class NameTagOptions {

    private boolean factionInformation = true;
    private boolean highlightFaction = true;
    private boolean highlightAlliance = true;
    private boolean additionalBlacklist = true;
    private boolean additionalContract = true;
    private boolean additionalHouseban = true;
    private boolean additionalWanted = true;
}
