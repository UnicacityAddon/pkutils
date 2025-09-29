package de.rettichlp.pkutils.mixin.client;

import de.rettichlp.pkutils.common.gui.MainOptionsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void addCustomButton(CallbackInfo ci) {
        int centerX = this.width / 2;
        int customY = (this.height / 4) - 20;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("PKUtils Settings"), button ->
                this.client.setScreen(new MainOptionsScreen())
        ).dimensions(centerX - 102, customY, 204, 20).build());
    }
}
