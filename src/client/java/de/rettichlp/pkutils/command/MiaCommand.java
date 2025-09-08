package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.models.Faction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.models.Faction.fromDisplayName;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "mia")
public class MiaCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("faction", greedyString())
                        .suggests((context, builder) -> suggestMatching(stream(Faction.values())
                                .map(Faction::getDisplayName), builder))
                        .executes(context -> {
                            String input = getString(context, "faction");
                            fromDisplayName(input).ifPresentOrElse(faction -> {
                                networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName());
                            }, () -> {
                                sendModMessage("Die Fraktion" + input + " konnte nicht gefunden werden.", false);
                            });

                            return 1;
                        })
                )
                .executes(context -> {
                    String playerName = player.getName().getString();
                    Faction faction = storage.getFaction(playerName);
                    networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName());
                    return 1;
                });
    }
}
