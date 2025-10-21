package de.rettichlp.pkutils.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.networkHandler;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilsService;
import static java.util.Objects.isNull;
import static net.minecraft.screen.slot.SlotActionType.PICKUP;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Unique
    private static final long A_BUY_DELAY = 150;

    @Unique
    private boolean isABuyProcessing = false;

    @Inject(
            method = "onSlotClick",
            at = @At("HEAD")
    )
    private void onSlotClickMixin(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient()) {
            return;
        }

        ScreenHandler handler = (ScreenHandler) (Object) this;

        if (!storage.isABuyEnabled() || slotIndex < 0 || slotIndex >= handler.slots.size() || actionType != PICKUP || this.isABuyProcessing) {
            return;
        }

        int aBuyAmount = storage.getABuyAmount();
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        Slot clickedSlot = handler.slots.get(slotIndex);
        ItemStack itemStack = clickedSlot.getStack();

        if (aBuyAmount <= 1 || isNull(interactionManager) || itemStack.isEmpty()) {
            return;
        }

        Int2ObjectMap<ItemStack> stackMap = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < handler.slots.size(); i++) {
            stackMap.put(i, handler.getSlot(i).getStack().copy());
        }

        Packet<ServerPlayPacketListener> packet = new ClickSlotC2SPacket(
                handler.syncId,
                handler.getRevision(),
                clickedSlot.id,
                0,
                PICKUP,
                itemStack,
                stackMap);

        this.isABuyProcessing = true;

        for (int i = 1; i < aBuyAmount; i++) {
            utilsService.delayedAction(() -> {
                // check if the same inventory is still open
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                if (isNull(currentScreen)) {
                    return;
                }

                networkHandler.sendPacket(packet);
            }, A_BUY_DELAY * i);
        }

        utilsService.delayedAction(() -> this.isABuyProcessing = false, A_BUY_DELAY * (aBuyAmount + 1));
    }
}
