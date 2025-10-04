package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import static net.minecraft.text.Text.translatable;
import static net.minecraft.util.Formatting.byName;

@Getter
@AllArgsConstructor
public enum Color {

    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_AQUA,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE;

    public MutableText getDisplayName() {
        return translatable("pkutils.options.text.color." + this.name().toLowerCase()).formatted(getFormatting());
    }

    public Formatting getFormatting() {
        return byName(name());
    }
}
