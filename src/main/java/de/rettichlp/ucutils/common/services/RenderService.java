package de.rettichlp.ucutils.common.services;

import de.rettichlp.ucutils.common.configuration.options.Options;
import de.rettichlp.ucutils.common.gui.screens.components.CyclingButtonEntry;
import de.rettichlp.ucutils.common.gui.screens.components.ToggleButtonWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;

public class RenderService {

    public static final int TEXT_BOX_PADDING = 3;

    public Color getSecondaryColor(@NotNull Color color) {
        return new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2, 100);
    }

    public <E extends CyclingButtonEntry> void addCyclingButton(@NotNull LinearLayout widget,
                                                                Component name,
                                                                E[] values,
                                                                Function<E, Component> displayNameFunction,
                                                                BiConsumer<Options, E> onValueChange,
                                                                @NotNull Function<Options, E> currentValue,
                                                                int width) {
        CycleButton<E> cycleButton = CycleButton.builder(displayNameFunction, currentValue.apply(configuration.getOptions()))
                .withValues(values)
                .withTooltip(CyclingButtonEntry::getTooltip)
                .create(name, (_, value) -> onValueChange.accept(configuration.getOptions(), value));

        cycleButton.setWidth(width);
        widget.addChild(cycleButton);
    }

    public ToggleButtonWidget addToggleButton(@NotNull LinearLayout widget,
                                Component name,
                                Component tooltip,
                                BiConsumer<Options, Boolean> onPress,
                                @NotNull Function<Options, Boolean> currentValue,
                                int width) {
        ToggleButtonWidget toggleButton = new ToggleButtonWidget(name, value -> onPress.accept(configuration.getOptions(), value), currentValue.apply(configuration.getOptions()));

        toggleButton.setWidth(width);
        toggleButton.setTooltip(Tooltip.create(tooltip));

        widget.addChild(toggleButton);
        return toggleButton;
    }

    public static @NonNull MutableComponent keyValue(String key, String value) {
        return keyValue(key, literal(value));
    }

    public static @NonNull MutableComponent keyValue(String key, Component value) {
        return keyValue(literal(key).withColor(GRAY), value);
    }

    public static @NonNull MutableComponent keyValue(@NonNull MutableComponent key, String value) {
        return keyValue(key, literal(value));
    }

    public static @NonNull MutableComponent keyValue(@NonNull MutableComponent key, Component value) {
        return empty()
                .append(key.withColor(GRAY))
                .append(literal(":").withColor(DARK_GRAY)).append(SPACE)
                .append(value);
    }
}
