package de.rettichlp.pkutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.syncService;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static boolean consentScreenAttempted = false;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (consentScreenAttempted) {
            return;
        }

        consentScreenAttempted = true;

        if (utilService.dataUsageConfirmed()) {
            LOGGER.info("Data usage confirmed, proceeding with sync...");
            sync();
        } else {
            PopupScreen dataUsageConfirmationScreen = new PopupScreen.Builder(this, empty().append(of("PKUtils")).append(" ").append(translatable("mco.terms.sentence.2")))
                    .message(translatable("pkutils.screen.data_usage_confirmation.message"))
                    .button(translatable("mco.terms.buttons.agree"), button -> {
                        utilService.updateDataUsageConfirmedUID();
                        sync();
                        close();
                    })
                    .button(translatable("mco.terms.buttons.disagree"), button -> close())
                    .build();

            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(dataUsageConfirmationScreen));
        }
    }

    @Unique
    private void sync() {
        // sync faction members
        syncService.syncFactionMembersWithApi();
        // sync blacklist reasons
        syncService.syncBlacklistReasonsFromApi();
        // check for updates
        syncService.checkForUpdates();

        // login to PKUtils API
        api.postUserRegister();
    }
}
