package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@PKUtilsCommand(label = "adropmoney")
public class ADropMoneyCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    sendCommands(List.of("bank abbuchen 16000", "dropmoney", "bank einzahlen 16000"));
                    return 1;
                });
    }
}
