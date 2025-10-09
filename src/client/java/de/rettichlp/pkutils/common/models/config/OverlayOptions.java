package de.rettichlp.pkutils.common.models.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.text.Text;

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
    public enum CarLockedStyle {

        DEFAULT(translatable("pkutils.options.overlay.car.locked.style.value.default")),
        MINIMALISTIC(translatable("pkutils.options.overlay.car.locked.style.value.minimalistic"));

        private final Text displayName;
    }
}
