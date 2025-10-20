package de.rettichlp.pkutils.command;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static de.rettichlp.pkutils.PKUtils.*;


@PKUtilsCommand(label = "abuy")
public class ABuyCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(ClientCommandManager.argument("amount", integer(1,300))
                        .executes(context -> {
                            int amount = context.getArgument("amount", Integer.class);

                            storage.setABuyAmount(amount);
                            sendModMessage("ABuy auf x" + amount + " eingestellt.", false);
                            return 1;
                        }))
                .executes(context -> {
                    if (storage.getABuyAmount() == 0){
                        sendModMessage("/abuy <amount>", false);
                        return 1;
                    }

                    storage.setABuyAmount(0);
                    sendModMessage("ABuy wurde zurückgesetzt.", false);
                    return 1;
                });
    }
}
