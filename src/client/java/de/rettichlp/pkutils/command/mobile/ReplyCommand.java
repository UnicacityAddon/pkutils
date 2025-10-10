package de.rettichlp.pkutils.command.mobile;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@PKUtilsCommand(label = "reply", aliases = "r")
public class ReplyCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("message", greedyString())
                        .executes(context -> {
                            String message = getString(context, "message");

                            int lastReceivedSmsNumber = storage.getLastReceivedSmsNumber();

                            if (lastReceivedSmsNumber < 0) {
                                sendModMessage("Kein SMS-Empfänger gefunden.", false);
                                return 1;
                            }

                            sendCommand("sms " + lastReceivedSmsNumber + " " + message);

                            return 1;
                        }));
    }
}
