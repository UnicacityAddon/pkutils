package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.services.HudService.NotificationType.DEFAULT;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.W_CHAT;

@PKUtilsCommand(label = "ww")
public class ToggleWChatCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    Storage.ToggledChat newState = storage.getToggledChat() == W_CHAT ? NONE : W_CHAT;
                    storage.setToggledChat(newState);
                    hudService.sendNotification(newState.getToggleMessage(), DEFAULT);
                    return 1;
                });
    }
}
