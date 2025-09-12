package de.rettichlp.pkutils.common.models.config;

import de.rettichlp.pkutils.common.models.Faction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static de.rettichlp.pkutils.common.models.Faction.NULL;

@Data
public class MainConfig {

    private List<TodoEntry> todos = new ArrayList<>();
    private Faction allianceFaction = NULL;
}
