package de.rettichlp.pkutils.common.configuration.options;

import de.rettichlp.pkutils.common.gui.options.components.CyclingButtonEntry;
import de.rettichlp.pkutils.common.models.PersonalUseEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.rettichlp.pkutils.common.configuration.options.Options.ReinforcementType.UNICACITYADDON;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
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
    private final CarOptions car = new CarOptions();
    private final OverlayOptions overlay = new OverlayOptions();

    private ReinforcementType reinforcementType = UNICACITYADDON;
    private boolean customSounds = true;

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum ReinforcementType implements CyclingButtonEntry {

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

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Tooltip getTooltip() {
            return Tooltip.of(this.displayName);
        }
    }
}
