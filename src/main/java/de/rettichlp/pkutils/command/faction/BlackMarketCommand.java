package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.util.Comparator.comparingDouble;
import static net.minecraft.text.Text.empty;

@PKUtilsCommand(label = "blackmarket", aliases = "schwarzmarkt")
public class BlackMarketCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    player.sendMessage(empty(), false);

                    sendModMessage("Schwarzmarkt Orte:", false);
                    storage.getBlackMarkets().stream()
                            .sorted(comparingDouble(value -> value.getType().getBlockPos().getSquaredDistance(player.getBlockPos())))
                            .forEach(blackMarket -> sendModMessage(blackMarket.getText(), false));

                    player.sendMessage(empty(), false);

                    return 1;
                });
    }
}
