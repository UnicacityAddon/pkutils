package de.rettichlp.pkutils.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static net.minecraft.item.Items.GLASS_BOTTLE;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Unique
    private final static List<BlockPos> SHOP_LOCATIONS = List.of(
            new BlockPos(45, 69, 200),
            new BlockPos(1049, 69, -189)
    );

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (player.getMainHandStack().isOf(GLASS_BOTTLE) && isNearShop()) {
            // cancel drop
            cir.setReturnValue(null);

            // execute command
            networkHandler.sendChatCommand("sell pfand");
        }
    }

    @Unique
    private boolean isNearShop() {
        BlockPos playerPos = player.getBlockPos();
        return SHOP_LOCATIONS.stream()
                .anyMatch(blockPos -> playerPos.isWithinDistance(blockPos, 10));
    }
}
