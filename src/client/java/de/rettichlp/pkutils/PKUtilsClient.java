package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.registry.Registry;
import de.rettichlp.pkutils.common.services.ActivityService;
import de.rettichlp.pkutils.common.services.FactionService;
import de.rettichlp.pkutils.common.services.SyncService;
import de.rettichlp.pkutils.common.storage.Storage;
import de.rettichlp.pkutils.listener.impl.faction.BombListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.TimeUnit;

public class PKUtilsClient implements ClientModInitializer {

    public static final Storage storage = new Storage();

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static ActivityService activityService;
    public static FactionService factionService;
    public static SyncService syncService;

    private final Registry registry = new Registry();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        activityService = new ActivityService();
        factionService = new FactionService();
        syncService = new SyncService();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraftClient) -> minecraftClient.execute(() -> {
            assert minecraftClient.player != null; // cannot be null at this point
            player = minecraftClient.player;
            networkHandler = minecraftClient.player.networkHandler;

            this.registry.registerListeners();
        }));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            this.registry.registerCommands(dispatcher);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (BombListener.bombTimerActive) {
                MinecraftClient client = MinecraftClient.getInstance();
                long elapsedTime = System.currentTimeMillis() - BombListener.bombPlantedTimestamp;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;

                Text timerText = Text.empty()
                        .append(Text.literal("Bombe: ").formatted(Formatting.RED, Formatting.BOLD))
                        .append(Text.literal(BombListener.bombLocation).formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal(" | ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.format("%02d:%02d", minutes, seconds)).formatted(Formatting.RED, Formatting.BOLD));

                int textWidth = client.textRenderer.getWidth(timerText);
                int x = (client.getWindow().getScaledWidth() - textWidth) / 2;
                int y = 15; // Position auf der Y-Achse

                drawContext.drawTextWithShadow(client.textRenderer, timerText, x, y, 0xFFFFFF);
            }
        });
    }
}
