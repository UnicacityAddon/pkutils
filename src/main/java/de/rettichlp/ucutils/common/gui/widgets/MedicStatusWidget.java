package de.rettichlp.ucutils.common.gui.widgets;

import de.rettichlp.therettingtoncompanion.gui.options.list.TRCOptionsList;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.AbstractTRCTextWidget;
import de.rettichlp.therettingtoncompanion.gui.widgets.base.WidgetConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.common.services.RenderService.keyValue;
import static java.lang.String.valueOf;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class MedicStatusWidget extends AbstractTRCTextWidget<MedicStatusWidget.Configuration> {

    private static final int WARNING_STATUS = 6;

    @Override
    public Component text() {
        int medicStatus = storage.getMedicStatus();

        MutableComponent value = literal(valueOf(medicStatus));
        if (medicStatus == WARNING_STATUS) {
            value = value.append(" ⚠️");
        }

        return keyValue(translatable("ucutils.options.widgets.medic_status.label"), value);
    }

    @Override
    public @Nullable String getRegistryName() {
        return "medic_status";
    }

    @Override
    public Component getLabel() {
        return translatable("ucutils.options.widgets.medic_status.options.name");
    }

    @Override
    public Component getTooltip() {
        return translatable("ucutils.options.widgets.medic_status.options.tooltip");
    }

    @Override
    public void addOptions(@NonNull TRCOptionsList optionsList) {}

    @Override
    public boolean isVisible() {
        // visible if in the position options screen to allow positioning
        return super.isVisible() && (storage.getMedicStatus() > 0 || isWidgetPositionScreen());
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Configuration extends WidgetConfiguration {}
}
