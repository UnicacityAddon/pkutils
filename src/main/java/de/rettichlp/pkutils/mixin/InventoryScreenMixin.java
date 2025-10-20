package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.PKUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.storage;


@Mixin(ScreenHandler.class)
public class InventoryScreenMixin {
    @Final

    @Inject(
            method = "onSlotClick",
            at = @At("HEAD")
    )
    private void onSlotClickMixin(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler) (Object) this;

        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot clickedSlot = handler.slots.get(slotIndex);
            ItemStack stack = clickedSlot.getStack();

            if (stack.isEmpty() && actionType != SlotActionType.PICKUP) {
                return;
            }

            int amount = storage.getABuyAmount();
            if (amount == 0) {
                return;
            }

            Int2ObjectMap<ItemStack> stackMap = new Int2ObjectOpenHashMap<>();
            for (int i = 0; i < handler.slots.size(); i++) {
                stackMap.put(i, handler.getSlot(i).getStack().copy());
            }

            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                    handler.syncId,
                    handler.getRevision(),
                    clickedSlot.id,
                    0,
                    SlotActionType.PICKUP,
                    stack,
                    stackMap);

            for (int i = 1; i < amount; i++) {
                PKUtils.api.delayedAction(() -> {
                    PKUtils.networkHandler.sendPacket(packet);
                }, 150L * i); // Maybe change delay. | 150L * i no lag but a bit slow.
            }
            storage.setABuyAmount(0);

        }
    }
}