package de.rettichlp.pkutils.common.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.command.ADropMoneyCommand;
import de.rettichlp.pkutils.command.ModCommand;
import de.rettichlp.pkutils.command.SyncCommand;
import de.rettichlp.pkutils.command.activity.CheckActivityCommand;
import de.rettichlp.pkutils.command.activity.ClearActivityCommand;
import de.rettichlp.pkutils.command.chat.ToggleDChatCommand;
import de.rettichlp.pkutils.command.chat.ToggleFChatCommand;
import de.rettichlp.pkutils.command.chat.ToggleWChatCommand;
import de.rettichlp.pkutils.command.faction.WSUCommand;
import de.rettichlp.pkutils.command.mobile.ACallCommand;
import de.rettichlp.pkutils.command.mobile.ASMSCommand;
import de.rettichlp.pkutils.command.money.DepositCommand;
import de.rettichlp.pkutils.command.money.RichTaxesCommand;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.listener.ITickListener;
import de.rettichlp.pkutils.listener.impl.CommandSendListener;
import de.rettichlp.pkutils.listener.impl.HudListener;
import de.rettichlp.pkutils.listener.impl.SyncListener;
import de.rettichlp.pkutils.listener.impl.business.BusinessListener;
import de.rettichlp.pkutils.listener.impl.faction.BlacklistListener;
import de.rettichlp.pkutils.listener.impl.faction.BombListener;
import de.rettichlp.pkutils.listener.impl.faction.FactionListener;
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
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.Objects.isNull;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Registry {

    private final Set<Class<?>> commands = Set.of(
            ACallCommand.class,
            ADropMoneyCommand.class,
            ASMSCommand.class,
            CheckActivityCommand.class,
            ClearActivityCommand.class,
            DepositCommand.class,
            ModCommand.class,
            RichTaxesCommand.class,
            SyncCommand.class,
            ToggleDChatCommand.class,
            ToggleFChatCommand.class,
            ToggleWChatCommand.class,
            WSUCommand.class
    );

    private final Set<Class<?>> listeners = Set.of(
            // business
            BusinessListener.class,
            // faction
            BombListener.class,
            BlacklistListener.class,
            FactionListener.class,
            HudListener.class,
            WantedListener.class,
            // job
            FisherListener.class,
            GarbageManListener.class,
            LumberjackListener.class,
            TransportListener.class,
            // other
            CommandSendListener.class,
            DepositCommand.class,
            RichTaxesCommand.class, // TODO find better solution for this
            SyncListener.class
    );

    private BlockPos lastPlayerPos = null;
    private boolean initialized = false;

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
            return;
        }

        for (Class<?> listenerClass : this.listeners /*ClassIndex.getAnnotated(PKUtilsListener.class)*/) {
            try {
                PKUtilsBase listenerInstance = (PKUtilsBase) listenerClass.getConstructor().newInstance();

                if (listenerInstance instanceof ICommandSendListener iCommandSendListener) {
                    ClientSendMessageEvents.ALLOW_COMMAND.register(iCommandSendListener::onCommandSend);
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
