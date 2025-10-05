package de.rettichlp.pkutils.common.models.config;

import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.Job;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.rettichlp.pkutils.common.models.Faction.NULL;
import java.util.Map;

@Data
public class MainConfig {

    private final Map<Job, LocalDateTime> jobCooldownEndTimes = new HashMap<>();
    private List<TodoEntry> todos = new ArrayList<>();
    private Faction allianceFaction = NULL;
    private Options options = new Options();
}
