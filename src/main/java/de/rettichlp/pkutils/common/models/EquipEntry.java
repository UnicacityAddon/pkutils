package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;

import static java.util.Arrays.stream;

public record EquipEntry(String id, Instant timeStamp, Type type) {

    @Getter
    @AllArgsConstructor
    public enum Type {

        DONUT("Donut"),
        CUFFS("Handschellen"),
        PEPPER_SPRAY("Pfefferspray"),
        KEVLAR_LIGHT("Kevlar (Leicht)"),
        MASK("Maske"),
        P_69("P-69"),
        SCATTER_3("Scatter-3"),
        TASER("Taser");

        private final String displayName;

        public @NotNull String getSuccessMessage() {
            return "Equip '" + this.displayName + "' wurde getrackt!";
        }

        public static @NotNull Optional<Type> fromDisplayName(String displayName) {
            return stream(values())
                    .filter(type -> displayName.equals(type.getDisplayName()))
                    .findFirst();
        }
    }
}
