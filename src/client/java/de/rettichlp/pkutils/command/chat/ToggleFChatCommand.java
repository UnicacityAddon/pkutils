package de.rettichlp.pkutils.command.chat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.F_CHAT;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;

@PKUtilsCommand(label = "ff")
public class ToggleFChatCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    Storage.ToggledChat newState = storage.getToggledChat() == F_CHAT ? NONE : F_CHAT;
                    storage.setToggledChat(newState);
                    hudService.sendInfoNotification(newState.getToggleMessage());
                    return 1;
                });
    }
}
