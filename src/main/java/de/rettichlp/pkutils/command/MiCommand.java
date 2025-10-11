package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Faction.fromDisplayName;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "mi")
public class MiCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("faction", greedyString())
                        .suggests((context, builder) -> suggestMatching(stream(Faction.values())
                                .map(Faction::getDisplayName), builder))
                        .executes(context -> {
                            String input = getString(context, "faction");
                            fromDisplayName(input).ifPresentOrElse(faction -> {
                                sendCommand("memberinfo " + faction.getMemberInfoCommandName());
                            }, () -> {
                                sendModMessage("Die Fraktion " + input + " konnte nicht gefunden werden.", false);
                            });

                            return 1;
                        })
                )
                .executes(context -> {
                    String playerName = player.getGameProfile().getName();
                    Faction faction = storage.getFaction(playerName);
                    sendCommand("memberinfo " + faction.getMemberInfoCommandName());
                    return 1;
                });
    }
}
