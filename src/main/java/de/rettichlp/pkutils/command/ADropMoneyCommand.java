package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.rettichlp.pkutils.PKUtils.*;

@PKUtilsCommand(label = "adropmoney")
public class ADropMoneyCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.SIDEBAR;

                    if (player.getScoreboard().getScore(ScoreHolder.fromName("§9Geld§8:"), player.getScoreboard().getObjectiveForSlot(slot)) == null) {
                        sendModMessage("§cEs wurde kein Geldtransport-Job gefunden.", true);
                        LOGGER.warn("ADropMoney: Scoreboard not found. ERROR: null");
                        return 1;
                    }

                    int dropamount = player.getScoreboard().getScore(ScoreHolder.fromName("§9Geld§8:"),
                            player.getScoreboard().getObjectiveForSlot(slot)).getScore();
                    if (dropamount <= configuration.getMoneyBankAmount()){
                        sendCommands(List.of("bank abbuchen " + dropamount, "dropmoney", "bank einzahlen " + dropamount));
                        return 1;
                    }
                    if (dropamount > 0){
                        int playermoney = configuration.getMoneyBankAmount();
                        sendCommands(List.of("bank abbuchen " + playermoney, "dropmoney", "bank einzahlen " + playermoney, "adropmoney"));
                    }
                    return 1;
                });
    }
}
