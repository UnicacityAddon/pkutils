package de.rettichlp.ucutils.common.gui.widgets;

import de.rettichlp.therettingtoncompanion.gui.ICycleButtonValue;
import de.rettichlp.therettingtoncompanion.gui.options.list.TRCOptionsList;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.AbstractTRCTextWidget;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.WidgetConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.common.gui.widgets.CarLockedWidget.Style.MINIMALISTIC;
import static java.util.Arrays.asList;
import static net.minecraft.client.gui.components.Tooltip.create;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GOLD;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.GREEN;

public class CarLockedWidget extends AbstractTRCTextWidget<CarLockedWidget.Configuration> {

    @Override
    public Component text() {
        return getWidgetConfiguration().getStyle() == MINIMALISTIC
                ? (storage.isCarLocked() ? literal("🔒").withColor(GREEN) : literal("🔓").withColor(GOLD))
                : empty()
                .append(translatable("ucutils.options.widgets.car_locked.label").withColor(GRAY))
                .append(literal(":").withColor(DARK_GRAY)).append(" ")
                .append(storage.isCarLocked()
                        ? translatable("ucutils.options.widgets.car_locked.locked").withColor(GREEN)
                        : translatable("ucutils.options.widgets.car_locked.unlocked").withColor(GOLD));
    }

    @Override
    public @Nullable String getRegistryName() {
        return "car_locked";
    }

    @Override
    public Component getLabel() {
        return translatable("ucutils.options.widgets.car_locked.options.name");
    }

    @Override
    public Component getTooltip() {
        return translatable("ucutils.options.widgets.car_locked.options.tooltip");
    }

    @Override
    public void addOptions(@NonNull TRCOptionsList optionsList) {
        optionsList.addCycleButton(translatable("ucutils.options.widgets.car_locked.options.style.name"), create(translatable("ucutils.options.widgets.car_locked.options.style.tooltip")), getWidgetConfiguration().getStyle(), asList(Style.values()), (_, value) -> getWidgetConfiguration().setStyle(value));
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements ICycleButtonValue {

        DEFAULT(translatable("ucutils.options.widgets.car_locked.options.style.value.default.name"), translatable("ucutils.options.widgets.car_locked.options.style.value.default.tooltip")),
        MINIMALISTIC(translatable("ucutils.options.widgets.car_locked.options.style.value.minimalistic.name"), translatable("ucutils.options.widgets.car_locked.options.style.value.minimalistic.tooltip"));

        private final Component name;
        private final Component tooltip;

        @Override
        public Component value() {
            return this.name;
        }

        @Contract(value = " -> new", pure = true)
        @Override
        public @NonNull Tooltip tooltip() {
            return create(this.tooltip);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends WidgetConfiguration {

        private Style style = MINIMALISTIC;
    }
}
