package de.rettichlp.pkutils.common.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.command.ADropMoneyCommand;
import de.rettichlp.pkutils.command.MiCommand;
import de.rettichlp.pkutils.command.MiaCommand;
import de.rettichlp.pkutils.command.ModCommand;
import de.rettichlp.pkutils.command.SyncCommand;
import de.rettichlp.pkutils.command.TodoCommand;
import de.rettichlp.pkutils.command.chat.ToggleDChatCommand;
import de.rettichlp.pkutils.command.chat.ToggleFChatCommand;
import de.rettichlp.pkutils.command.chat.ToggleWChatCommand;
import de.rettichlp.pkutils.command.faction.ASetBlacklistCommand;
import de.rettichlp.pkutils.command.faction.ActivityCommand;
import de.rettichlp.pkutils.command.faction.BlackMarketCommand;
import de.rettichlp.pkutils.command.faction.EquippedCommand;
import de.rettichlp.pkutils.command.faction.MinusPointsCommand;
import de.rettichlp.pkutils.command.faction.PersonalUseCommand;
import de.rettichlp.pkutils.command.mobile.ACallCommand;
import de.rettichlp.pkutils.command.mobile.ASMSCommand;
import de.rettichlp.pkutils.command.mobile.ReplyCommand;
import de.rettichlp.pkutils.command.money.DepositCommand;
import de.rettichlp.pkutils.command.money.RichTaxesCommand;
import de.rettichlp.pkutils.common.models.Sound;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;
import de.rettichlp.pkutils.listener.IBlockRightClickListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IEnterVehicleListener;
import de.rettichlp.pkutils.listener.IEntityRenderListener;
import de.rettichlp.pkutils.listener.IEntityRightClickListener;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.listener.IScreenOpenListener;
import de.rettichlp.pkutils.listener.ITickListener;
import de.rettichlp.pkutils.listener.callback.PlayerEnterVehicleCallback;
import de.rettichlp.pkutils.listener.impl.CarListener;
import de.rettichlp.pkutils.listener.impl.CommandSendListener;
import de.rettichlp.pkutils.listener.impl.EconomyService;
import de.rettichlp.pkutils.listener.impl.MobileListener;
import de.rettichlp.pkutils.listener.impl.PlayerListener;
import de.rettichlp.pkutils.listener.impl.RenderListener;
import de.rettichlp.pkutils.listener.impl.SyncListener;
import de.rettichlp.pkutils.listener.impl.faction.BlacklistListener;
import de.rettichlp.pkutils.listener.impl.faction.BombListener;
import de.rettichlp.pkutils.listener.impl.faction.ContractListener;
import de.rettichlp.pkutils.listener.impl.faction.FactionDoorListener;
import de.rettichlp.pkutils.listener.impl.faction.FactionListener;
import de.rettichlp.pkutils.listener.impl.faction.MedicListener;
import de.rettichlp.pkutils.listener.impl.faction.ServiceListener;
import de.rettichlp.pkutils.listener.impl.faction.WantedListener;
import de.rettichlp.pkutils.listener.impl.job.FisherListener;
import de.rettichlp.pkutils.listener.impl.job.GarbageManListener;
import de.rettichlp.pkutils.listener.impl.job.JobListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.entity.effect.StatusEffects.ABSORPTION;
import static net.minecraft.registry.Registries.SOUND_EVENT;
import static net.minecraft.registry.Registry.register;
import static net.minecraft.util.ActionResult.PASS;

public class Registry {

    @Deprecated(since = "1.6.3")
    private final Set<Class<?>> commands = Set.of(
            ACallCommand.class,
            ADropMoneyCommand.class,
            ASMSCommand.class,
            ASetBlacklistCommand.class,
            ActivityCommand.class,
            BlackMarketCommand.class,
            DepositCommand.class,
            EquippedCommand.class,
            MiCommand.class,
            MiaCommand.class,
            MinusPointsCommand.class,
            ModCommand.class,
            PersonalUseCommand.class,
            ReplyCommand.class,
            RichTaxesCommand.class,
            SyncCommand.class,
            TodoCommand.class,
            ToggleDChatCommand.class,
            ToggleFChatCommand.class,
            ToggleWChatCommand.class
    );

    @Deprecated(since = "1.6.3")
    private final Set<Class<?>> listeners = Set.of(
            BlacklistListener.class,
            BombListener.class,
            CarListener.class,
            CommandSendListener.class,
            ContractListener.class,
            DepositCommand.class,
            EconomyService.class,
            FactionDoorListener.class,
            FactionListener.class,
            FisherListener.class,
            GarbageManListener.class,
            JobListener.class,
            MedicListener.class,
            MobileListener.class,
            PersonalUseCommand.class,
            PlayerListener.class,
            RenderListener.class,
            ServiceListener.class,
            SyncListener.class,
            WantedListener.class
    );

    private final Set<PKUtilsBase> listenerInstances = getListenerInstances();

    private boolean initialized = false;
    private BlockPos lastPlayerPos = null;
    private boolean lastAbsorptionState = false;

    public void registerSounds() {
        for (Sound value : Sound.values()) {
            register(SOUND_EVENT, value.getIdentifier(), value.getSoundEvent());
        }
    }

    public void registerCommands(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Class<?> commandClass : this.commands /*ClassIndex.getAnnotated(PKUtilsCommand.class)*/) {
            try {
                PKUtilsCommand annotation = commandClass.getAnnotation(PKUtilsCommand.class);
                String label = annotation.label();
                CommandBase commandInstance = (CommandBase) commandClass.getConstructor().newInstance();

                LiteralArgumentBuilder<FabricClientCommandSource> node = literal(label);
                LiteralArgumentBuilder<FabricClientCommandSource> enrichedNode = commandInstance.execute(node);
                dispatcher.register(enrichedNode);

                // alias handling
                for (String alias : annotation.aliases()) {
                    LiteralArgumentBuilder<FabricClientCommandSource> aliasNode = literal(alias);
                    LiteralArgumentBuilder<FabricClientCommandSource> enrichedAliasNode = commandInstance.execute(aliasNode);
                    dispatcher.register(enrichedAliasNode);
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                LOGGER.error("Error while registering command: {}", commandClass.getName(), e.getCause());
            }
        }
    }

    public void registerListeners() {
        if (this.initialized) {
            LOGGER.warn("Listeners already registered");
            return;
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String rawMessage = message.getString();

            // handle navi spot reached
            if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                getListenersImplementing(INaviSpotReachedListener.class).forEach(INaviSpotReachedListener::onNaviSpotReached);
            }

            // handle message receiving
            boolean showMessage = getListenersImplementing(IMessageReceiveListener.class).stream()
                    .allMatch(iMessageReceiveListener -> iMessageReceiveListener.onMessageReceive(message, rawMessage));

            if (!showMessage) {
                LOGGER.info("PKUtils hidden message: {}", message.getString());
            }

            return showMessage;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(s -> {
            boolean sendMessage = getListenersImplementing(IMessageSendListener.class).stream()
                    .allMatch(iMessageSendListener -> iMessageSendListener.onMessageSend(s));

            if (!sendMessage) {
                LOGGER.info("PKUtils blocked message sending: {}", s);
            }

            return sendMessage;
        });

        ClientSendMessageEvents.ALLOW_COMMAND.register(commandWithoutPrefix -> {
            boolean executeCommand = getListenersImplementing(ICommandSendListener.class).stream()
                    .allMatch(iCommandSendListener -> iCommandSendListener.onCommandSend(commandWithoutPrefix));

            if (!executeCommand) {
                LOGGER.info("PKUtils blocked command execution: /{}", commandWithoutPrefix);
            }

            return executeCommand;
        });

        ClientTickEvents.END_CLIENT_TICK.register((server) -> {
            // handle tick
            getListenersImplementing(ITickListener.class).forEach(ITickListener::onTick);

            // handle on move
            BlockPos blockPos = player.getBlockPos();
            if (isNull(this.lastPlayerPos) || !this.lastPlayerPos.equals(blockPos)) {
                this.lastPlayerPos = blockPos;
                getListenersImplementing(IMoveListener.class).forEach(iMoveListener -> iMoveListener.onMove(blockPos));
            }

            // handle absorption
            boolean hasAbsorption = ofNullable(player)
                    .map(clientPlayerEntity -> clientPlayerEntity.hasStatusEffect(ABSORPTION))
                    .orElse(false);

            if (!this.lastAbsorptionState && hasAbsorption) {
                getListenersImplementing(IAbsorptionGetListener.class).forEach(IAbsorptionGetListener::onAbsorptionGet);
            }

            this.lastAbsorptionState = hasAbsorption;
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            getListenersImplementing(IHudRenderListener.class).forEach(iHudRenderListener -> iHudRenderListener.onHudRender(drawContext, tickCounter));
        });

        PlayerEnterVehicleCallback.EVENT.register(vehicle -> {
            getListenersImplementing(IEnterVehicleListener.class).forEach(iEnterVehicleListener -> iEnterVehicleListener.onEnterVehicle(vehicle));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            getListenersImplementing(IScreenOpenListener.class).forEach(iScreenOpenListener -> iScreenOpenListener.onScreenOpen(screen, scaledWidth, scaledHeight));
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            getListenersImplementing(IBlockRightClickListener.class).forEach(iBlockRightClickListener -> iBlockRightClickListener.onBlockRightClick(world, hand, hitResult));
            return PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            getListenersImplementing(IEntityRightClickListener.class).forEach(iEntityRightClickListener -> iEntityRightClickListener.onEntityRightClick(world, hand, entity, hitResult));
            return PASS;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            getListenersImplementing(IEntityRenderListener.class).forEach(iEntityRenderListener -> iEntityRenderListener.onEntityRender(context));
        });

        // prevent multiple registrations of listeners
        this.initialized = true;
    }

    private @NotNull Set<PKUtilsBase> getListenerInstances() {
        return this.listeners.stream()
                .map(listenerClass -> {
                    try {
                        return (PKUtilsBase) listenerClass.getConstructor().newInstance();
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                        LOGGER.error("Error while registering listener: {}", listenerClass.getName(), e.getCause());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private <T> Set<T> getListenersImplementing(Class<T> listenerInterface) {
        return !storage.isPunicaKitty() ? emptySet() : this.listenerInstances.stream()
                .filter(listenerInterface::isInstance)
                .map(listenerInterface::cast)
                .collect(toSet());
    }
}
