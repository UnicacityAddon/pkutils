package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.api.Api;
import de.rettichlp.pkutils.common.configuration.Configuration;
import de.rettichlp.pkutils.common.registry.Registry;
import de.rettichlp.pkutils.common.services.CommandService;
import de.rettichlp.pkutils.common.services.FactionService;
import de.rettichlp.pkutils.common.services.MessageService;
import de.rettichlp.pkutils.common.services.NotificationService;
import de.rettichlp.pkutils.common.services.RenderService;
import de.rettichlp.pkutils.common.services.SyncService;
import de.rettichlp.pkutils.common.services.UtilsService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Boolean.getBoolean;
import static java.util.Objects.isNull;

public class PKUtils implements ModInitializer {

    public static final String MOD_ID = "pkutils";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CommandService commandService = new CommandService();
    public static final FactionService factionService = new FactionService();
    public static final MessageService messageService = new MessageService();
    public static final NotificationService notificationService = new NotificationService();
    public static final RenderService renderService = new RenderService();
    public static final SyncService syncService = new SyncService();
    public static final UtilsService utilsService = new UtilsService();

    public static final Api api = new Api();
    public static final Storage storage = new Storage();
    public static final Configuration configuration = new Configuration().loadFromFile();

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    private final Registry registry = new Registry();

    @Override
    public void onInitialize() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        this.registry.registerSounds();

        // sync faction members
        syncService.syncFactionMembersWithApi();
        // sync blacklist reasons
        syncService.syncBlacklistReasonsFromApi();
        // check for updates
        syncService.checkForUpdates();

        // login to PKUtils API
        api.postUserRegister();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            player = client.player;
            networkHandler = handler;

            storage.setPunicaKitty(isPunicaKitty());
            client.execute(() -> {
                this.registry.registerListeners();
                renderService.initializeWidgets();
            });
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> this.registry.registerCommands(dispatcher));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configuration.saveToFile());
    }

    private boolean isPunicaKitty() {
        if (getBoolean("fabric.development")) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (isNull(networkHandler)) {
            LOGGER.warn("Not connected to PunicaKitty: Network handler is null");
            return false;
        }

        String addressString = networkHandler.getConnection().getAddress().toString(); // tcp.punicakitty.de./50.114.4.xxx:25565
        // for LabyMod players, there is no dot at the end of the domain
        if (!addressString.matches("tcp\\.punicakitty\\.de\\.?/50\\.114\\.4\\.\\d+:25565")) {
            LOGGER.warn("Not connected to PunicaKitty: {}", addressString);
            return false;
        }

        return true;
    }
}
