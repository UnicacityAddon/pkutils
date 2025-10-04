package de.rettichlp.pkutils.common.models;

import lombok.Data;

import static de.rettichlp.pkutils.common.models.Purity.BEST;

@Data
public class PersonalUseEntry {

    private final InventoryItem inventoryItem;
    private Purity purity = BEST;
    private int amount = 0;
}
