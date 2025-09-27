package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Arrays.stream;
import static net.minecraft.item.Items.FERN;
import static net.minecraft.item.Items.GUNPOWDER;
import static net.minecraft.item.Items.IRON_INGOT;
import static net.minecraft.item.Items.LIME_DYE;
import static net.minecraft.item.Items.PINK_DYE;
import static net.minecraft.item.Items.QUARTZ;
import static net.minecraft.item.Items.SUGAR;
import static net.minecraft.item.Items.WITHER_SKELETON_SKULL;

@Getter
@AllArgsConstructor
public enum InventoryItem {

    // drugs
    POWDER("Pulver", true, SUGAR),
    HERBS("Kräuter", true, FERN),
    CRYSTALS("Kristalle", true, QUARTZ),
    GRAB_BAG("Wundertüte", true, LIME_DYE),

    // medical
    COUGH_SYRUP("Hustensaft", false, PINK_DYE),
    PAINKILLERS("Schmerzmittel", false, PINK_DYE),
    ANTIBIOTICS("Antibiotika", false, PINK_DYE),

    // other
    MASK("Maske", false, WITHER_SKELETON_SKULL),
    GUN_POWDER("Schwarzpulver", false, GUNPOWDER),
    IRON("Eisen", false, IRON_INGOT);

    private final String displayName;
    private final boolean drugBankItem;
    private final Item item;

    @Contract(" -> new")
    public @NotNull ItemStack getItemStack() {
        return new ItemStack(this.item);
    }

    public static @NotNull Optional<InventoryItem> fromDisplayName(String displayName) {
        return stream(values())
                .filter(inventoryItem -> inventoryItem.getDisplayName().equals(displayName))
                .findFirst();
    }
}
