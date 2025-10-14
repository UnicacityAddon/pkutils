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
import de.rettichlp.pkutils.command.faction.ScreenshotCommand;
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
import de.rettichlp.pkutils.listener.impl.CommandListener;
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
import de.rettichlp.pkutils.listener.impl.faction.EmergencyServiceListener;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.entity.effect.StatusEffects.ABSORPTION;
import static net.minecraft.registry.Registries.SOUND_EVENT;
import static net.minecraft.registry.Registry.register;
import static net.minecraft.util.ActionResult.PASS;

public class Registry {

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
            ScreenshotCommand.class,
            SyncCommand.class,
            TodoCommand.class,
            ToggleDChatCommand.class,
            ToggleFChatCommand.class,
            ToggleWChatCommand.class
    );

    private final Set<Class<?>> listeners = Set.of(
            BlacklistListener.class,
            BombListener.class,
            CarListener.class,
            CommandListener.class,
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
            EmergencyServiceListener.class,
            SyncListener.class,
            WantedListener.class
    );

    private BlockPos lastPlayerPos = null;
    private boolean lastAbsorptionState = false;
    private boolean initialized = false;

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

        for (Class<?> listenerClass : this.listeners /*ClassIndex.getAnnotated(PKUtilsListener.class)*/) {
            try {
                PKUtilsBase listenerInstance = (PKUtilsBase) listenerClass.getConstructor().newInstance();

                if (listenerInstance instanceof IAbsorptionGetListener iAbsorptionGetListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        if (storage.isPunicaKitty()) {
                            boolean hasAbsorption = ofNullable(player)
                                    .map(clientPlayerEntity -> clientPlayerEntity.hasStatusEffect(ABSORPTION))
                                    .orElse(false);

                            if (!this.lastAbsorptionState && hasAbsorption) {
                                iAbsorptionGetListener.onAbsorptionGet();
                            }

                            this.lastAbsorptionState = hasAbsorption;
                        }
                    });
                }

                if (listenerInstance instanceof IBlockRightClickListener iBlockRightClickListener) {
                    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                        if (storage.isPunicaKitty()) {
                            iBlockRightClickListener.onBlockRightClick(world, hand, hitResult);
                        }

                        return PASS;
                    });
                }

                if (listenerInstance instanceof ICommandSendListener iCommandSendListener) {
                    ClientSendMessageEvents.ALLOW_COMMAND.register(s -> !storage.isPunicaKitty() || iCommandSendListener.onCommandSend(s));
                }

                if (listenerInstance instanceof IEnterVehicleListener iEnterVehicleListener) {
                    PlayerEnterVehicleCallback.EVENT.register(vehicle -> {
                        if (storage.isPunicaKitty()) {
                            iEnterVehicleListener.onEnterVehicle(vehicle);
                        }
                    });
                }

                if (listenerInstance instanceof IEntityRenderListener iEntityRenderListener) {
                    WorldRenderEvents.AFTER_ENTITIES.register(context -> {
                        if (storage.isPunicaKitty()) {
                            iEntityRenderListener.onEntityRender(context);
                        }
                    });
                }

                if (listenerInstance instanceof IEntityRightClickListener iEntityRightClickListener) {
                    UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                        if (storage.isPunicaKitty()) {
                            iEntityRightClickListener.onEntityRightClick(world, hand, entity, hitResult);
                        }

                        return PASS;
                    });
                }

                if (listenerInstance instanceof IHudRenderListener iHudRenderListener) {
                    HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
                        if (storage.isPunicaKitty()) {
                            iHudRenderListener.onHudRender(drawContext, tickCounter);
                        }
                    });
                }

                if (listenerInstance instanceof IMessageReceiveListener iMessageReceiveListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        if (!storage.isPunicaKitty()) {
                            return true;
                        }

                        String rawMessage = message.getString();
                        boolean showMessage = iMessageReceiveListener.onMessageReceive(message, rawMessage);

                        if (!showMessage) {
                            LOGGER.info("Hide message ({}): {}", listenerClass.getSimpleName(), rawMessage);
                        }

                        return showMessage;
                    });
                }

                if (listenerInstance instanceof IMessageSendListener iMessageSendListener) {
                    ClientSendMessageEvents.ALLOW_CHAT.register(s -> !storage.isPunicaKitty() || iMessageSendListener.onMessageSend(s));
                }

                if (listenerInstance instanceof IMoveListener iMoveListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> ofNullable(player)
                            .filter(clientPlayerEntity -> storage.isPunicaKitty())
                            .map(Entity::getBlockPos)
                            .ifPresent(blockPos -> {
                                if (isNull(this.lastPlayerPos) || !this.lastPlayerPos.equals(blockPos)) {
                                    this.lastPlayerPos = blockPos;
                                    iMoveListener.onMove(blockPos);
                                }
                            }));
                }

                if (listenerInstance instanceof INaviSpotReachedListener iNaviSpotReachedListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        if (storage.isPunicaKitty()) {
                            String rawMessage = message.getString();
                            if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                                iNaviSpotReachedListener.onNaviSpotReached();
                            }
                        }

                        return true;
                    });
                }

                if (listenerInstance instanceof IScreenOpenListener iScreenOpenListener) {
                    ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                        if (storage.isPunicaKitty()) {
                            iScreenOpenListener.onScreenOpen(screen, scaledWidth, scaledHeight);
                        }
                    });
                }

                if (listenerInstance instanceof ITickListener iTickListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        if (storage.isPunicaKitty()) {
                            iTickListener.onTick();
                        }
                    });
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                LOGGER.error("Error while registering listener: {}", listenerClass.getName(), e.getCause());
            }
        }

        // prevent multiple registrations of listeners
        this.initialized = true;
    }
}
