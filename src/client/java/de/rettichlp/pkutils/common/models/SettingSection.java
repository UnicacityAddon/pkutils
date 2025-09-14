package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@AllArgsConstructor
@Getter
public enum SettingSection {
    PERSONAL_USE("Eigenbedarf");

    private final String displayName;
}
