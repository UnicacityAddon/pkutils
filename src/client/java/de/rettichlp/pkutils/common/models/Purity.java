package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.text.Text;

import static net.minecraft.text.Text.translatable;
import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@Getter
@AllArgsConstructor
public enum Purity {

    BEST(translatable("pkutils.purity.best").formatted(DARK_GREEN)),
    GOOD(translatable("pkutils.purity.good").formatted(GREEN)),
    MEDIUM(translatable("pkutils.purity.medium").formatted(YELLOW)),
    BAD(translatable("pkutils.purity.bad").formatted(RED));

    private final Text displayName;
}
