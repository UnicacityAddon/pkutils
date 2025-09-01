package de.rettichlp.pkutils.common.api.schema;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum ActivityType {

    ARREST("Verhaftung"),
    ARREST_KILL("Verhaftung (Kill)"),
    EMERGENCY_SERVICE("Notruf"),
    LOGIN("Login"),
    LOGOUT("Logout"),
    MAJOR_EVENT("Großereignis"),
    PARK_TICKET("Strafzettel"),
    REINFORCEMENT("Reinforcement");

    private final String displayMessage;

    public @NotNull String getSuccessMessage() {
        return "Aktivität '" + this.displayMessage + "' wurde getrackt!";
    }
}
