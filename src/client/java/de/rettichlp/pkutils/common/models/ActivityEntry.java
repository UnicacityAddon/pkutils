package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record ActivityEntry(String id, Instant timeStamp, Type type) {

    @Getter
    @AllArgsConstructor
    public enum Type {

        ARREST("Verhaftung"),
        ARREST_KILL("Verhaftung (Kill)"),
        EMERGENCY_SERVICE("Notruf"),
        MAJOR_EVENT("Großereignis"),
        PARK_TICKET("Strafzettel"),
        REINFORCEMENT("Reinforcement"),
        REVIVE("Wiederbelebung");

        private final String displayMessage;

        public @NotNull String getSuccessMessage() {
            return "Aktivität '" + this.displayMessage + "' wurde getrackt!";
        }
    }
}
