package de.rettichlp.pkutils.mixin.client;

import de.rettichlp.pkutils.listener.callback.PlayerEnterVehicleCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("RETURN"))
    private void onStartRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) { // only for successful start riding
            Entity self = (Entity) (Object) this;
            if (self instanceof ServerPlayerEntity) {
                PlayerEnterVehicleCallback.EVENT.invoker().onEnter(vehicle);
            }
        }
    }
}
