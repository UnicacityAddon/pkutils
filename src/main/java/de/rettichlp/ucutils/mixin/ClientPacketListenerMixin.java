package de.rettichlp.ucutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.ucutils.UCUtils.LOGGER;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static java.util.Objects.requireNonNull;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.DARK_RED;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;
import static net.minecraft.network.chat.TextColor.YELLOW;
import static net.minecraft.world.entity.Entity.RemovalReason.DISCARDED;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetEntityData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V",
                     shift = AFTER))
    private void ucutils$handleSetEntityDataInvoke(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (!storage.isUnicaCity()) {
            return;
        }

        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        Entity entity = world.getEntity(packet.id());
        if (entity == null) {
            return;
        }

        Vec3 entityPos = entity.position();

        // already notified check
        if (entityPos.equals(storage.getDealerPosition()) || entityPos.equals(storage.getBlackMarketPosition()) || entityPos.equals(storage.getBloodDealerPosition()) || entityPos.equals(storage.getSummerTreasurePosition())) {
            return;
        }

        switch (entity) {
            case Villager villager when villager.hasCustomName() -> {
                String customNameString = requireNonNull(villager.getCustomName()).getString();
                switch (customNameString) {
                    case "Dealer" -> {
                        storage.setBlackMarketPosition(entityPos);
                        player.sendSystemMessage(empty()
                                .append(literal("[").withColor(DARK_GRAY))
                                .append(literal("Dealer").withColor(RED))
                                .append(literal("] ").withColor(DARK_GRAY))
                                .append(literal("Der Dealer ist in der Nähe!").withColor(GRAY)));
                    }
                    case "Schwarzmarkt" -> {
                        storage.setDealerPosition(entityPos);
                        player.sendSystemMessage(empty()
                                .append(literal("[").withColor(DARK_GRAY))
                                .append(literal("Schwarzmarkt").withColor(RED))
                                .append(literal("] ").withColor(DARK_GRAY))
                                .append(literal("Der Schwarzmarkt ist in der Nähe!").withColor(GRAY)));
                    }
                    case "Bluthändler" -> {
                        storage.setBloodDealerPosition(entityPos);
                        player.sendSystemMessage(empty()
                                .append(literal("[").withColor(DARK_GRAY))
                                .append(literal("Bluthändler").withColor(DARK_RED))
                                .append(literal("] ").withColor(DARK_GRAY))
                                .append(literal("Der Bluthändler ist in der Nähe!").withColor(GRAY)));
                    }
                }
            }
            case Dolphin dolphin when configuration.getOptions().miscellaneous().hideDolphins() && !dolphin.hasCustomName() -> {
                world.removeEntity(dolphin.getId(), DISCARDED);
                LOGGER.info("Dolphin from fisher chest was not spawned at {}", entityPos);
            }
            case ArmorStand armorStand when armorStand.hasCustomName() -> {
                String customNameString = requireNonNull(armorStand.getCustomName()).getString();
                if (customNameString.equals("Sommerschatz")) {
                    storage.setSummerTreasurePosition(entityPos);
                    player.sendSystemMessage(empty()
                            .append(literal("[").withColor(DARK_GRAY))
                            .append(literal("Schatzsuche").withColor(YELLOW))
                            .append(literal("] ").withColor(DARK_GRAY))
                            .append(literal("Der Sommerschatz ist in der Nähe!").withColor(GRAY)));
                }
            }
            default -> {
            }
        }
    }
}
