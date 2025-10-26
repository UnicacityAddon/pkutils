package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

import static de.rettichlp.pkutils.common.models.Faction.FBI;
import static de.rettichlp.pkutils.common.models.Faction.POLIZEI;
import static de.rettichlp.pkutils.common.models.Faction.RETTUNGSDIENST;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

public record ActivityEntry(String id, Instant timeStamp, Type type) {

    @Getter
    @AllArgsConstructor
    public enum Type {

        ARREST("Verhaftung", List.of(FBI, POLIZEI)),
        ARREST_KILL("Verhaftung (Kill)", List.of(FBI, POLIZEI)),
        EMERGENCY_SERVICE("Notruf", List.of(POLIZEI, RETTUNGSDIENST)),
        MAJOR_EVENT("Großereignis"),
        PARK_TICKET("Strafzettel", List.of(POLIZEI)),
        REINFORCEMENT("Reinforcement"),
        REVIVE("Wiederbelebung", List.of(RETTUNGSDIENST));

        private final String displayName;
        private final List<Faction> allowedFactions;

        Type(String displayName) {
            this.displayName = displayName;
            this.allowedFactions = asList(Faction.values());
        }

        public @NotNull String getSuccessMessage() {
            return "Aktivität '" + this.displayName + "' wurde getrackt!";
        }

        public boolean isAllowedForFaction(Faction faction) {
            return nonNull(faction) && this.allowedFactions.contains(faction);
        }
    }
}
