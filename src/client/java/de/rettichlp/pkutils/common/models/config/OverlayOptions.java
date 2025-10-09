package de.rettichlp.pkutils.common.models.config;

import de.rettichlp.pkutils.common.gui.options.components.CyclingButtonEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.common.models.config.OverlayOptions.CarLockedStyle.DEFAULT;
import static net.minecraft.text.Text.translatable;

@Getter
@Setter
@Accessors(fluent = true)
public class OverlayOptions {

    private boolean dateTime = true;
    private boolean payDay = true;
    private boolean payDaySalary = true;
    private boolean payDayExperience = true;
    private boolean carLocked = true;
    private CarLockedStyle carLockedStyle = DEFAULT;

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum CarLockedStyle implements CyclingButtonEntry {

        DEFAULT("pkutils.options.overlay.car.locked.style.value.default"),
        MINIMALISTIC("pkutils.options.overlay.car.locked.style.value.minimalistic");

        private final String translationKey;

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Text getDisplayName() {
            return translatable(this.translationKey + ".name");
        }

        @Contract(" -> new")
        @Override
        public @NotNull Tooltip getTooltip() {
            return Tooltip.of(translatable(this.translationKey + ".description"));
        }
    }
}
