package de.rettichlp.pkutils.common.api.schema;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Activity(String id, Instant timeStamp, Type type) {

    @AllArgsConstructor
    public enum Type {

        ARREST("Verhaftung"),
        ARREST_KILL("Verhaftung (Kill)"),
        EMERGENCY_SERVICE("Notruf"),
        MAJOR_EVENT("Großereignis"),
        PARK_TICKET("Strafzettel"),
        REINFORCEMENT("Reinforcement");

        private final String displayMessage;

        public @NotNull String getSuccessMessage() {
            return "Aktivität '" + this.displayMessage + "' wurde getrackt!";
        }
    }
}
