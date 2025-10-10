package de.rettichlp.pkutils.command.chat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.Storage.ToggledChat.W_CHAT;

@PKUtilsCommand(label = "ww")
public class ToggleWChatCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    Storage.ToggledChat newState = storage.getToggledChat() == W_CHAT ? NONE : W_CHAT;
                    storage.setToggledChat(newState);
                    notificationService.sendInfoNotification(newState.getToggleMessage());
                    return 1;
                });
    }
}
