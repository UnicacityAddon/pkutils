package de.rettichlp.pkutils.common.models.config;

import de.rettichlp.pkutils.common.models.PersonalUseEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static de.rettichlp.pkutils.common.models.config.Options.ReinforcementType.UNICACITYADDON;
import static net.minecraft.text.Text.*;
import static net.minecraft.text.Text.empty;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.RED;

@Getter
@Setter
@Accessors(fluent = true)
public class Options {

    private final NameTagOptions nameTag = new NameTagOptions();
    private final List<PersonalUseEntry> personalUse = new ArrayList<>();

    private ReinforcementType reinforcementType = UNICACITYADDON;

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum ReinforcementType {

        UCUTILS(empty()
                .append(of("UC").copy().formatted(DARK_AQUA))
                .append(of("Utils").copy().formatted(AQUA))),
        UNICACITYADDON(empty()
                .append(of("U").copy().formatted(BLUE))
                .append(of("nica"))
                .append(of("C").copy().formatted(RED))
                .append(of("ity"))
                .append(of("A").copy().formatted(BLUE))
                .append(of("ddon")));

        private final Text displayName;
    }
}
