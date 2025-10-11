package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.common.models.ScreenshotType;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.common.models.ScreenshotType.OTHER;
import static de.rettichlp.pkutils.common.models.ScreenshotType.fromDisplayName;
import static java.lang.Thread.sleep;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void keepChatOpenAfterCommand(String message, boolean addToHistory, CallbackInfo ci) {
        String[] messageParts = message.split(" ");
        if (messageParts.length >= 3 && message.startsWith("/screenshot type ")) {
            String screenshotTypeString = messageParts[2].toLowerCase();
            ScreenshotType screenshotType = fromDisplayName(screenshotTypeString).orElse(OTHER);
            screenshotType.take(file -> notificationService.sendInfoNotification("Screenshot gespeichert: '" + file.getName() + "'"));

            try {
                sleep(30);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted while trying to keep chat open", e);
            }
        }
    }
}
