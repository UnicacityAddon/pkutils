package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.api.Api;
import de.rettichlp.pkutils.common.registry.Registry;
import de.rettichlp.pkutils.common.services.ConfigService;
import de.rettichlp.pkutils.common.services.FactionService;
import de.rettichlp.pkutils.common.services.HudService;
import de.rettichlp.pkutils.common.services.SyncService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class PKUtilsClient implements ClientModInitializer {

    public static final Api api = new Api();
    public static final Storage storage = new Storage();

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static ConfigService configService;
    public static FactionService factionService;
    public static HudService hudService;
    public static SyncService syncService;

    private final Registry registry = new Registry();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        configService = new ConfigService();
        factionService = new FactionService();
        hudService = new HudService();
        syncService = new SyncService();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            player = client.player;
            networkHandler = handler;

            this.registry.registerListeners();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            this.registry.registerCommands(dispatcher);
        });
    }
}
