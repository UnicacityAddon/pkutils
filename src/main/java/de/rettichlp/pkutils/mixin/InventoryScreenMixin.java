package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.PKUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.storage;


@Mixin(ScreenHandler.class)
public class InventoryScreenMixin {

    @Inject(
            method = "onSlotClick",
            at = @At("HEAD")
    )
    private void onSlotClickMixin(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler)(Object)this;

        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot clickedSlot = handler.slots.get(slotIndex);
            ItemStack stack = clickedSlot.getStack();

            if (!stack.isEmpty() && actionType == SlotActionType.PICKUP) {
                int amount = storage.getABuyAmount();
                if (amount == 0){
                    return;
                }

                ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
                for (int i = 0; i < amount; i++){
                    interactionManager.clickSlot(handler.syncId, clickedSlot.id, button, SlotActionType.PICKUP, PKUtils.player); //TODO: Fix and find solution.
                }

            }
        }
    }
}
