package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "mi")
public class MiCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(ClientCommandManager.argument("faction", greedyString())
                        .suggests((context, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();

                            Arrays.stream(Faction.values())
                                    .map(Faction::getDisplayName)
                                    .filter(name -> name.toLowerCase().contains(remaining))
                                    .forEach(builder::suggest);

                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String input = getString(context, "faction");
                            Optional<Faction> factionOpt = Faction.fromDisplayName(input);

                            if (factionOpt.isEmpty()) {
                                sendModMessage("Die Fraktion" + input + " konnte nicht gefunden werden.", false);
                                return 0;
                            }

                            Faction faction = factionOpt.get();
                            networkHandler.sendChatCommand("memberinfo " + faction.getMemberInfoCommandName());
                            return 1;
                        })
                )
                .executes(context -> {
                    String playerName = player.getName().getString();
                    Faction faction = storage.getFaction(playerName);

                    networkHandler.sendChatCommand("memberinfo " + faction.getMemberInfoCommandName());
                    return 1;
                });
    }
}
