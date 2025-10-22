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

    private static final Text WIDGETS_CAR_LOCKED_OPTIONS_NAME = translatable("pkutils.options.widgets.car_locked.options.name");
    private static final Text WIDGETS_CAR_LOCKED_OPTIONS_TOOLTIP = translatable("pkutils.options.widgets.car_locked.options.tooltip");
    private static final Text WIDGETS_CAR_LOCKED_OPTIONS_STYLE_NAME = translatable("pkutils.options.widgets.car_locked.options.style.name");

    @Override
    public Text text() {
        return getWidgetConfiguration().getStyle() == MINIMALISTIC
                ? (storage.isCarLocked() ? of("🔒").copy().formatted(GREEN) : of("🔓").copy().formatted(GOLD))
                : empty()
                .append(of("Fahrzeug").copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(storage.isCarLocked() ? of("zu").copy().formatted(GREEN) : of("offen").copy().formatted(GOLD));
    }

    @Override
    public Text getDisplayName() {
        return WIDGETS_CAR_LOCKED_OPTIONS_NAME;
    }

    @Override
    public Text getTooltip() {
        return WIDGETS_CAR_LOCKED_OPTIONS_TOOLTIP;
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements CyclingButtonEntry {

        DEFAULT(translatable("pkutils.options.widgets.car_locked.options.style.value.default.name"), translatable("pkutils.options.widgets.car_locked.options.style.value.default.tooltip")),
        MINIMALISTIC(translatable("pkutils.options.widgets.car_locked.options.style.value.minimalistic.name"), translatable("pkutils.options.widgets.car_locked.options.style.value.minimalistic.tooltip"));

        private final Text name;
        private final Text tooltip;

        @Override
        public @NotNull Text getDisplayName() {
            return this.name;
        }

        @Override
        public @NotNull Tooltip getTooltip() {
            return Tooltip.of(this.tooltip);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends PKUtilsWidgetConfiguration implements IOptionWidget {

        private Style style = MINIMALISTIC;

        @Override
        public Widget optionsWidget() {
            return CyclingButtonWidget.builder(Style::getDisplayName)
                    .values(Style.values())
                    .initially(this.style)
                    .tooltip(Style::getTooltip)
                    .build(WIDGETS_CAR_LOCKED_OPTIONS_STYLE_NAME, (button, style) -> this.style = style);
        }
    }
}
