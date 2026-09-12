package de.rettichlp.ucutils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.notificationService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static java.awt.Color.WHITE;
import static java.util.Optional.ofNullable;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.network.chat.TextColor.BLUE;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GOLD;
import static net.minecraft.network.chat.TextColor.YELLOW;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Unique
    private static final MutableComponent A_DUTY_PREFIX = empty()
            .append(literal("[").withColor(DARK_GRAY)
                    .append(literal("UC").withColor(BLUE))
                    .append(literal("]").withColor(DARK_GRAY)));

    @Unique
    private static final MutableComponent BUILD_MODE_PREFIX = empty()
            .append(literal("[").withColor(DARK_GRAY)
                    .append(literal("B").withColor(YELLOW))
                    .append(literal("]").withColor(DARK_GRAY)));

    @Unique
    private static final MutableComponent REPORT_PREFIX = empty()
            .append(literal("[").withColor(DARK_GRAY)
                    .append(literal("R").withColor(GOLD))
                    .append(literal("]").withColor(DARK_GRAY)));

    @Inject(method = "handlePlayerInfoRemove",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z",
                     shift = AFTER))
    private void ucutils$handlePlayerInfoRemoveInvoke(ClientboundPlayerInfoRemovePacket packet,
                                                      CallbackInfo ci,
                                                      @Local(name = "info") PlayerInfo info) {
        if (!storage.isUnicaCity()) {
            return;
        }

        if (!configuration.getOptions().notification().joinQuit()) {
            return;
        }

        sendChangeNotification(ofNullable(info.getTabListDisplayName()).orElseGet(() -> literal(info.getProfile().name())), "ucutils.notification.player_quit");
    }

    @Inject(method = "handlePlayerInfoUpdate",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/screens/social/PlayerSocialManager;addPlayer(Lnet/minecraft/client/multiplayer/PlayerInfo;)V",
                     shift = AFTER))
    private void ucutils$handlePlayerInfoUpdateInvoke(ClientboundPlayerInfoUpdatePacket packet,
                                                      CallbackInfo ci,
                                                      @Local(name = "entry") ClientboundPlayerInfoUpdatePacket.Entry entry,
                                                      @Local(name = "playerInfo") @NonNull PlayerInfo playerInfo) {
        if (!storage.isUnicaCity()) {
            return;
        }

        if (!configuration.getOptions().notification().joinQuit()) {
            return;
        }

        sendChangeNotification(ofNullable(entry.displayName()).orElseGet(() -> literal(playerInfo.getProfile().name())), "ucutils.notification.player_join");
    }

    @Inject(method = "applyPlayerInfoUpdate",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/multiplayer/PlayerInfo;setTabListDisplayName(Lnet/minecraft/network/chat/Component;)V"))
    private void ucutils$applyPlayerInfoUpdateInvoke(ClientboundPlayerInfoUpdatePacket.Action action,
                                                     ClientboundPlayerInfoUpdatePacket.Entry entry,
                                                     PlayerInfo info,
                                                     CallbackInfo ci) {
        if (!storage.isUnicaCity()) {
            return;
        }

        if (!configuration.getOptions().notification().joinQuit()) {
            return;
        }

        Component previousDisplayName = info.getTabListDisplayName();
        Component currentDisplayName = entry.displayName();

        if (previousDisplayName == null || currentDisplayName == null) {
            return;
        }

        // handle report change
        if (configuration.getOptions().notification().report()) {
            if (!previousDisplayName.contains(REPORT_PREFIX) && currentDisplayName.contains(REPORT_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_enter_report");
                return;
            }

            if (previousDisplayName.contains(REPORT_PREFIX) && !currentDisplayName.contains(REPORT_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_leave_report");
                return;
            }
        }

        // handle build mode change
        if (configuration.getOptions().notification().buildMode()) {
            if (!previousDisplayName.contains(BUILD_MODE_PREFIX) && currentDisplayName.contains(BUILD_MODE_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_enter_buildmode");
                return;
            }

            if (previousDisplayName.contains(BUILD_MODE_PREFIX) && !currentDisplayName.contains(BUILD_MODE_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_leave_buildmode");
                return;
            }
        }

        // handle admin-duty change
        if (configuration.getOptions().notification().aDuty()) {
            if (!previousDisplayName.contains(A_DUTY_PREFIX) && currentDisplayName.contains(A_DUTY_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_enter_a_duty");
                return;
            }

            if (previousDisplayName.contains(A_DUTY_PREFIX) && !currentDisplayName.contains(A_DUTY_PREFIX)) {
                sendChangeNotification(currentDisplayName, "ucutils.notification.player_leave_a_duty");
                return;
            }
        }
    }

    @Unique
    private void sendChangeNotification(@NonNull Component displayName, String translationKey) {
        // skip NPC names
        if (displayName.getString().isEmpty()) {
            return;
        }

        // skip if player joined some seconds ago
        if (player.tickCount < 5 * 20) {
            return;
        }

        MutableComponent text = translatable(translationKey, displayName);
        notificationService.sendNotification(text, WHITE, 5000);
    }
}
