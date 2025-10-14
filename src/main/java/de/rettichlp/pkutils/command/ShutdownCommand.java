package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.common.models.ShutdownReason.CEMETERY;
import static de.rettichlp.pkutils.common.models.ShutdownReason.JAIL;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@PKUtilsCommand(label = "shutdown")
public class ShutdownCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("friedhof")
                        .executes(context -> {
                            CEMETERY.activate();
                            return 1;
                        }))
                .then(literal("gefängnis")
                        .executes(context -> {
                            JAIL.activate();
                            return 1;
                        }));
    }
}
