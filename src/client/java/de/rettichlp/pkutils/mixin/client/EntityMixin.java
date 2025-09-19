package de.rettichlp.pkutils.mixin.client;

import de.rettichlp.pkutils.listener.callback.PlayerEnterVehicleCallback;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.rettichlp.pkutils.PKUtilsClient.player;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("RETURN"))
    private void onStartRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        // only for successful start riding
        if (!cir.getReturnValue()) {
            return;
        }

        EntityLike self = (Entity) (Object) this;
        if (self.getUuid().equals(player.getUuid())) {
            PlayerEnterVehicleCallback.EVENT.invoker().onEnter(vehicle);
        }
    }
}
