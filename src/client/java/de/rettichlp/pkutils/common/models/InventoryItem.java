package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
@AllArgsConstructor
public enum InventoryItem {

    // drugs
    POWDER("Pulver", true),
    HERBS("Kräuter", true),
    CRYSTALS("Kristalle", true),
    GRAB_BAG("Wundertüte", true),

    // medical
    COUGH_SYRUP("Hustensaft", false),
    PAINKILLERS("Schmerzmittel", false),
    ANTIBIOTICS("Antibiotika", false),

    // other
    MASK("Maske", false),
    GUN_POWDER("Schwarzpulver", false),
    IRON("Eisen", false);

    private final String displayName;
    private final boolean drugBankItem;

    public static @NotNull Optional<InventoryItem> fromDisplayName(String displayName) {
        return stream(values())
                .filter(inventoryItem -> inventoryItem.getDisplayName().equals(displayName))
                .findFirst();
    }
}
