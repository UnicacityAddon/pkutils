package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.common.models.Faction.fromDisplayName;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "wsu", aliases = "wp")
public class AllianceCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("faction", greedyString())
                        .suggests((context, builder) -> suggestMatching(stream(Faction.values())
                                .map(Faction::getDisplayName), builder))
                        .executes(context -> {
                            String input = getString(context, "faction");
                            fromDisplayName(input).ifPresentOrElse(faction -> {
                                configService.edit(mainConfig -> mainConfig.setAllianceFaction(faction));
                                hudService.sendInfoNotification("Die Allianz-Fraktion wurde auf " + faction.getDisplayName() + " gesetzt.");
                            }, () -> sendModMessage("Die Fraktion" + input + " konnte nicht gefunden werden.", false));

                            return 1;
                        }));
    }
}
