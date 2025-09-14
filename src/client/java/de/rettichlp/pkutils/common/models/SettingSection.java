package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@AllArgsConstructor
@Getter
public enum SettingSection {
    PERSONAL_USE("Eigenbedarf");
    PERSONAL_USE("Eigenbedarf"),
    TEST("Test"),
    TEST2("Test2"),
    TEST3("Test3"),
    TEST4("Test4"),
    TEST5("Test"),
    TEST6("Test"),
    TEST7("Test"),
    TEST8("Test"),
    TEST9("Test"),
    TEST10("Test"),
    TEST11("Test"),
    TEST12("Test");

    private final String displayName;
}
