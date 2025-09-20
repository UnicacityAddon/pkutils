package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Getter
@AllArgsConstructor
public enum DrugType {
    WEED("Gras", new ItemStack(Items.FERN)),
    COCAINE("Koks", new ItemStack(Items.SUGAR)),
    METH("Meth", new ItemStack(Items.QUARTZ)),
    LSD("LSD", new ItemStack(Items.PINK_DYE));

    private final String displayName;
    private final ItemStack itemStack;
}
