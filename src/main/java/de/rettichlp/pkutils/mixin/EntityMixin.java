package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.listener.callback.PlayerEnterVehicleCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;

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

    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void onStopRiding(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof ClientPlayerEntity && self.hasVehicle() && self.getVehicle() instanceof MinecartEntity minecartEntity && storage.isPunicaKitty()) {
            storage.setMinecartEntityToHighlight(minecartEntity);
        }
    }
}
