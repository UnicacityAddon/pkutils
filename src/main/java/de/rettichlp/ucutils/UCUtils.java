package de.rettichlp.ucutils;

import de.rettichlp.ucutils.common.Storage;
import de.rettichlp.ucutils.common.api.Api;
import de.rettichlp.ucutils.common.configuration.Configuration;
import de.rettichlp.ucutils.common.registry.Registry;
import de.rettichlp.ucutils.common.services.CommandService;
import de.rettichlp.ucutils.common.services.MessageService;
import de.rettichlp.ucutils.common.services.NameTagService;
import de.rettichlp.ucutils.common.services.NotificationService;
import de.rettichlp.ucutils.common.services.RenderService;
import de.rettichlp.ucutils.common.services.ResyncableTimer;
import de.rettichlp.ucutils.common.services.SyncService;
import de.rettichlp.ucutils.common.services.UtilService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;

import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static java.lang.Boolean.getBoolean;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping;
import static net.minecraft.client.KeyMapping.Category.register;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.slf4j.LoggerFactory.getLogger;

public class UCUtils implements ModInitializer {

    public static final String MOD_ID = "ucutils";
    public static final String MOD_NAME = "UCUtils";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = getLogger(MOD_ID);

    public static final Api api = new Api();
    public static final Storage storage = new Storage();
    public static final Configuration configuration = new Configuration().loadFromFile();
    public static final ResyncableTimer synchronisedMinuteTimer = new ResyncableTimer(1, 1, MINUTES);

    public static final CommandService commandService = new CommandService();
    public static final MessageService messageService = new MessageService();
    public static final NameTagService nameTagService = new NameTagService();
    public static final NotificationService notificationService = new NotificationService();
    public static final RenderService renderService = new RenderService();
    public static final SyncService syncService = new SyncService();
    public static final UtilService utilService = new UtilService();

    public static final KeyMapping.Category KEY_CATEGORY = register(fromNamespaceAndPath(MOD_ID, "name"));
    public static final KeyMapping REINFORCEMENT_ACCEPT_KEY = registerKeyMapping(new KeyMapping("ucutils.key.reinforcement_accept", KEYSYM, GLFW_KEY_UNKNOWN, KEY_CATEGORY));

    public static LocalPlayer player;
    public static ClientPacketListener networkHandler;

    private static final String NEOPROTECT_ENTRYPOINT = "c970141b-0cca-4ad2-894b-21ac9c171cbe.shield.neoprotect.ovh";

    private final Registry registry = new Registry();

    @Override
    public void onInitialize() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        synchronisedMinuteTimer.start();

        syncService.syncFactionMembers();
        syncService.syncTeamMembers();

        this.registry.registerSounds();

        // add payday minute
        synchronisedMinuteTimer.add(_ -> {
            if (storage.isUnicaCity() && !nameTagService.isAfk(player.getPlainTextName())) {
                configuration.setMinutesSinceLastPayDay(configuration.getMinutesSinceLastPayDay() + 1);
            }
        });

        // show health for hydration bar sync
        synchronisedMinuteTimer.add(currentTick -> {
            // every 3 minutes starting from first (not third) minute
            if ((currentTick + 2) % 3 != 0) {
                return;
            }

            // delayed to wait for afk message if in same minute
            utilService.delayedAction(() -> {
                if (storage.isUnicaCity() && !nameTagService.isAfk(player.getPlainTextName()) && configuration.getOptions().miscellaneous().showHydration()) {
                    commandService.sendCommandWithHiddenOutput("health");
                }
            }, 50);
        });

        // asynchronously save every 10 minutes
        synchronisedMinuteTimer.add(currentTick -> {
            // every 10 minutes
            if (currentTick % 10 != 0) {
                return;
            }

            new Thread(configuration::saveToFile).start();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            player = client.player;
            networkHandler = handler;

            boolean isUnicaCity = isUnicaCity(handler);
            storage.setUnicaCity(isUnicaCity);
            storage.setJoinTimestamp(currentTimeMillis());
            if (isUnicaCity) {
                client.execute(() -> {
                    this.registry.registerListeners();
                    utilService.delayedAction(syncService::syncFactionSpecificData, 10000);
                    utilService.delayedAction(syncService::checkForUpdates, 15000);
                });
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> this.registry.registerCommands(dispatcher));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configuration.saveToFile());
    }

    private boolean isUnicaCity(ClientPacketListener networkHandler) {
        if (getBoolean("fabric.development") || !configuration.getOptions().checkUnicacityServer()) {
            return true;
        }

        if (isNull(networkHandler)) {
            LOGGER.warn("Not connected to UnicaCity: Network handler is null");
            return false;
        }

        String addressString = networkHandler.getConnection().getRemoteAddress().toString();
        // for LabyMod players, there is no dot at the end of the domain
        if (!addressString.matches(NEOPROTECT_ENTRYPOINT + "\\.?/\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+")) {
            LOGGER.warn("Not connected to UnicaCity: {}", addressString);
            return false;
        }

        LOGGER.info("Connected to UnicaCity: {}", addressString);
        return true;
    }
}
