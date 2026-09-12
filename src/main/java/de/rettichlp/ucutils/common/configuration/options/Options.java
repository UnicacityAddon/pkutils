package de.rettichlp.ucutils.common.configuration.options;

import de.rettichlp.ucutils.common.gui.screens.components.CyclingButtonEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static de.rettichlp.ucutils.common.configuration.options.Options.ReinforcementType.UNICACITYADDON;
import static net.minecraft.client.gui.components.Tooltip.create;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.AQUA;
import static net.minecraft.network.chat.TextColor.BLUE;
import static net.minecraft.network.chat.TextColor.DARK_AQUA;
import static net.minecraft.network.chat.TextColor.RED;
import static net.minecraft.world.item.Items.CHEST;
import static net.minecraft.world.item.Items.ENDER_CHEST;
import static net.minecraft.world.item.Items.TRAPPED_CHEST;

@Getter
@Setter
@Accessors(fluent = true)
public class Options {

    private final NameTagOptions nameTag = new NameTagOptions();
    private final ChatOptions chatOptions = new ChatOptions();
    private final CarOptions car = new CarOptions();
    private final SoundOptions sound = new SoundOptions();
    private final NotificationOptions notification = new NotificationOptions();
    private final MiscellaneousOptions miscellaneous = new MiscellaneousOptions();

    private boolean checkUnicacityServer = true;
    private ReinforcementType reinforcementType = UNICACITYADDON;
    private Set<String> autoCollectFromTrashCan = new HashSet<>(Set.of(CHEST.toString(), TRAPPED_CHEST.toString(), ENDER_CHEST.toString()));

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum ReinforcementType implements CyclingButtonEntry {

        UCUTILS(empty()
                .append(literal("UC").withColor(DARK_AQUA))
                .append(literal("Utils").withColor(AQUA))),
        UNICACITYADDON(empty()
                .append(literal("U").withColor(BLUE))
                .append(literal("nica"))
                .append(literal("C").withColor(RED))
                .append(literal("ity"))
                .append(literal("A").withColor(BLUE))
                .append(literal("ddon")));

        private final Component displayName;

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Tooltip getTooltip() {
            return create(this.displayName);
        }
    }
}
