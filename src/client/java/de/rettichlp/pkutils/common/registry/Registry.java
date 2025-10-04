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
import de.rettichlp.pkutils.command.faction.AllianceCommand;
import de.rettichlp.pkutils.command.faction.BlackMarketCommand;
import de.rettichlp.pkutils.command.faction.EquippedCommand;
import de.rettichlp.pkutils.command.faction.MinusPointsCommand;
import de.rettichlp.pkutils.command.faction.PersonalUseCommand;
import de.rettichlp.pkutils.command.mobile.ACallCommand;
import de.rettichlp.pkutils.command.mobile.ASMSCommand;
import de.rettichlp.pkutils.command.money.DepositCommand;
import de.rettichlp.pkutils.command.money.RichTaxesCommand;
import de.rettichlp.pkutils.common.models.Sound;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IEnterVehicleListener;
import de.rettichlp.pkutils.listener.IEntityRenderListener;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.listener.IScreenOpenListener;
import de.rettichlp.pkutils.listener.ITickListener;
import de.rettichlp.pkutils.listener.callback.PlayerEnterVehicleCallback;
import de.rettichlp.pkutils.listener.impl.AbsorptionListener;
import de.rettichlp.pkutils.listener.impl.BusinessListener;
import de.rettichlp.pkutils.listener.impl.CarListener;
import de.rettichlp.pkutils.listener.impl.CommandSendListener;
import de.rettichlp.pkutils.listener.impl.HudListener;
import de.rettichlp.pkutils.listener.impl.MoneyListener;
import de.rettichlp.pkutils.listener.impl.SyncListener;
import de.rettichlp.pkutils.listener.impl.faction.BlacklistListener;
import de.rettichlp.pkutils.listener.impl.faction.BombListener;
import de.rettichlp.pkutils.listener.impl.faction.ContractListener;
import de.rettichlp.pkutils.listener.impl.faction.FactionListener;
import de.rettichlp.pkutils.listener.impl.faction.HousebanListener;
import de.rettichlp.pkutils.listener.impl.faction.ReviveListener;
import de.rettichlp.pkutils.listener.impl.faction.ServiceListener;
import de.rettichlp.pkutils.listener.impl.faction.WantedListener;
import de.rettichlp.pkutils.listener.impl.job.FisherListener;
import de.rettichlp.pkutils.listener.impl.job.GarbageManListener;
import de.rettichlp.pkutils.listener.impl.job.LumberjackListener;
import de.rettichlp.pkutils.listener.impl.job.TransportListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.Objects.isNull;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.entity.effect.StatusEffects.ABSORPTION;
import static net.minecraft.registry.Registries.SOUND_EVENT;
import static net.minecraft.registry.Registry.register;

public class Registry {

    private final Set<Class<?>> commands = Set.of(
            ACallCommand.class,
            ADropMoneyCommand.class,
            ASMSCommand.class,
            ASetBlacklistCommand.class,
            ActivityCommand.class,
            AllianceCommand.class,
            BlackMarketCommand.class,
            DepositCommand.class,
            EquippedCommand.class,
            MiCommand.class,
            MiaCommand.class,
            MinusPointsCommand.class,
            ModCommand.class,
            PersonalUseCommand.class,
            RichTaxesCommand.class,
            SyncCommand.class,
            TodoCommand.class,
            ToggleDChatCommand.class,
            ToggleFChatCommand.class,
            ToggleWChatCommand.class
    );

    private final Set<Class<?>> listeners = Set.of(
            AbsorptionListener.class,
            BlacklistListener.class,
            BombListener.class,
            BusinessListener.class,
            CarListener.class,
            CommandSendListener.class,
            ContractListener.class,
            DepositCommand.class,
            FactionListener.class,
            FisherListener.class,
            GarbageManListener.class,
            HousebanListener.class,
            HudListener.class,
            LumberjackListener.class,
            MoneyListener.class,
            ReviveListener.class,
            ServiceListener.class,
            SyncListener.class,
            TransportListener.class,
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
        // ignore messages until the player is initialized
        if (player == null || networkHandler == null || this.initialized) {
            throw new IllegalStateException("Tried to register listeners too early");
        }

        for (Class<?> listenerClass : this.listeners /*ClassIndex.getAnnotated(PKUtilsListener.class)*/) {
            try {
                PKUtilsBase listenerInstance = (PKUtilsBase) listenerClass.getConstructor().newInstance();

                if (listenerInstance instanceof IAbsorptionGetListener iAbsorptionGetListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        boolean hasAbsorption = player.hasStatusEffect(ABSORPTION);

                        if (!this.lastAbsorptionState && hasAbsorption) {
                            iAbsorptionGetListener.onAbsorptionGet();
                        }

                        this.lastAbsorptionState = hasAbsorption;
                    });
                }

                if (listenerInstance instanceof ICommandSendListener iCommandSendListener) {
                    ClientSendMessageEvents.ALLOW_COMMAND.register(iCommandSendListener::onCommandSend);
                }

                if (listenerInstance instanceof IEnterVehicleListener iEnterVehicleListener) {
                    PlayerEnterVehicleCallback.EVENT.register(iEnterVehicleListener::onEnterVehicle);
                }

                if (listenerInstance instanceof IEntityRenderListener iEntityRenderListener) {
                    WorldRenderEvents.AFTER_ENTITIES.register(iEntityRenderListener::onEntityRender);
                }

                if (listenerInstance instanceof IHudRenderListener iHudRenderListener) {
                    HudRenderCallback.EVENT.register(iHudRenderListener::onHudRender);
                }

                if (listenerInstance instanceof IMessageReceiveListener iMessageReceiveListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        String rawMessage = message.getString();
                        return iMessageReceiveListener.onMessageReceive(message, rawMessage);
                    });
                }

                if (listenerInstance instanceof IMessageSendListener iMessageSendListener) {
                    ClientSendMessageEvents.ALLOW_CHAT.register(iMessageSendListener::onMessageSend);
                }

                if (listenerInstance instanceof IMoveListener iMoveListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        BlockPos blockPos = player.getBlockPos();
                        if (isNull(this.lastPlayerPos) || !this.lastPlayerPos.equals(blockPos)) {
                            this.lastPlayerPos = blockPos;
                            iMoveListener.onMove(blockPos);
                        }
                    });
                }

                if (listenerInstance instanceof INaviSpotReachedListener iNaviSpotReachedListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        String rawMessage = message.getString();
                        if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                            iNaviSpotReachedListener.onNaviSpotReached();
                        }

                        return true;
                    });
                }

                if (listenerInstance instanceof IScreenOpenListener iScreenOpenListener) {
                    ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> iScreenOpenListener.onScreenOpen(screen, scaledWidth, scaledHeight));
                }

                if (listenerInstance instanceof ITickListener iTickListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        iTickListener.onTick();
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
