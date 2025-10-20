package de.rettichlp.pkutils.common.gui.widgets;

import de.rettichlp.pkutils.common.gui.options.components.CyclingButtonEntry;
import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsTextWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.IOptionWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidgetConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.widgets.CarLockedWidget.Style.MINIMALISTIC;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;

@PKUtilsWidget(registryName = "car_locked", defaultX = 110.0, defaultY = 4.0)
public class CarLockedWidget extends AbstractPKUtilsTextWidget<CarLockedWidget.Configuration> {

    @Override
    public Text text() {
        return getWidgetConfiguration().getStyle() == MINIMALISTIC
                ? (storage.isCarLocked() ? of("🔒").copy().formatted(GREEN) : of("🔓").copy().formatted(GOLD))
                : empty()
                .append(of("Fahrzeug").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(storage.isCarLocked() ? of("zu").copy().formatted(GREEN) : of("offen").copy().formatted(GOLD));
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements CyclingButtonEntry {

        DEFAULT("pkutils.widget.car_locked.configuration.style.value.default"),
        MINIMALISTIC("pkutils.widget.car_locked.configuration.style.value.minimalistic");

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

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends PKUtilsWidgetConfiguration implements IOptionWidget {

        private Style style = MINIMALISTIC;

        @Override
        public Text sectionTitle() {
            return translatable("pkutils.options.overlay.car.locked.name");
        }

        @Override
        public Widget optionsWidget() {
            return CyclingButtonWidget.builder(Style::getDisplayName)
                    .values(Style.values())
                    .initially(this.style)
                    .tooltip(Style::getTooltip)
                    .build(of("text"), (button, style) -> this.style = style);
        }
    }
}
