package de.rettichlp.pkutils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.rettichlp.pkutils.common.gui.options.MainOptionsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.text.Text.of;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "initWidgets",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;ILnet/minecraft/client/gui/widget/Positioner;)Lnet/minecraft/client/gui/widget/Widget;",
                    ordinal = 0
            )
    )
    private void addCustomButton(CallbackInfo ci, @Local @NotNull GridWidget gridWidget, @Local GridWidget.@NotNull Adder adder) {
        ButtonWidget buttonWidget = ButtonWidget.builder(of("PKUtils Settings"), button -> this.client.setScreen(new MainOptionsScreen()))
                .width(204)
                .build();

        // the first item of the adder has a top margin of 50, so we add a negative top margin to compensate (46 for a small gap of 4)
        adder.add(buttonWidget, 2, gridWidget.copyPositioner().marginTop(50).marginBottom(-46));
    }
}
