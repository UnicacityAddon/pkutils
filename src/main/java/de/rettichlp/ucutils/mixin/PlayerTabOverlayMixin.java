package de.rettichlp.ucutils.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.rettichlp.ucutils.UCUtils.storage;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.RED;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void ucutils$getNameForDisplayReturn(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        if (!storage.isUnicaCity()) {
            return;
        }

        String playerName = info.getProfile().name();
        Component originText = cir.getReturnValue();

        MutableComponent text = null;

        boolean isWanted = storage.getWantedEntries().stream()
                .anyMatch(wantedEntry -> wantedEntry.getPlayerName().equals(playerName));

        if (isWanted) {
            text = literal(" 🔍").withColor(RED).withStyle(BOLD);
        }

        if (text != null) {
            cir.setReturnValue(originText.copy().append(SPACE).append(text));
        }
    }
}
