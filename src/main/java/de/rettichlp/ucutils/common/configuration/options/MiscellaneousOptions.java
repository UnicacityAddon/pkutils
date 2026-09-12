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

import static de.rettichlp.ucutils.common.configuration.options.MiscellaneousOptions.AtmInformationType.NONE;
import static de.rettichlp.ucutils.common.configuration.options.MiscellaneousOptions.HydrationTextureType.STYLE_1;
import static net.minecraft.client.gui.components.Tooltip.create;
import static net.minecraft.network.chat.Component.translatable;

@Getter
@Setter
@Accessors(fluent = true)
public class MiscellaneousOptions {

    private boolean showHydration = true;
    private HydrationTextureType hydrationTextureType = STYLE_1;
    private AtmInformationType atmInformationType = NONE;
    private boolean highlightCorpses = false; // feature rejected by UnicaCity team
    private boolean hideDolphins = false;
    private boolean blockMalleSound = false;
    private boolean autoSiren = true;

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum HydrationTextureType implements CyclingButtonEntry {

        STYLE_1(translatable("ucutils.options.hydration_texture.value.style_1.name"), translatable("ucutils.options.hydration_texture.value.style_1.tooltip")),
        STYLE_2(translatable("ucutils.options.hydration_texture.value.style_2.name"), translatable("ucutils.options.hydration_texture.value.style_2.tooltip")),
        STYLE_3(translatable("ucutils.options.hydration_texture.value.style_3.name"), translatable("ucutils.options.hydration_texture.value.style_3.tooltip"));

        private final Component displayName;
        private final Component tooltip;

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Tooltip getTooltip() {
            return create(this.tooltip);
        }
    }

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = false)
    public enum AtmInformationType implements CyclingButtonEntry {

        NONE(translatable("ucutils.options.atm_information.value.none.name"), translatable("ucutils.options.atm_information.value.none.tooltip")),
        F_BANK(translatable("ucutils.options.atm_information.value.f_bank.name"), translatable("ucutils.options.atm_information.value.f_bank.tooltip")),
        G_BANK(translatable("ucutils.options.atm_information.value.g_bank.name"), translatable("ucutils.options.atm_information.value.g_bank.tooltip")),
        BOTH(translatable("ucutils.options.atm_information.value.both.name"), translatable("ucutils.options.atm_information.value.both.tooltip"));

        private final Component displayName;
        private final Component tooltip;

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Tooltip getTooltip() {
            return create(this.tooltip);
        }
    }
}
