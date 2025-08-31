package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.regex.Pattern.compile;

@PKUtilsCommand(label = "einzahlen")
public class DepositCommand extends CommandBase implements IMessageReceiveListener {

    private int amount = 0;

    private static final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^ - Geld: (?<moneyAmount>\\d+)\\$$");

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {

        networkHandler.sendChatCommand("stats");

        delayedAction(() -> {

            if (this.amount == 0) {
                sendModMessage("Du hast kein Geld zum Einzahlen.", false);
                return;
            }

            networkHandler.sendChatCommand("bank einzahlen " + this.amount);

        }, 1000);

        return null;
    }

    @Override
    public boolean onMessageReceive(String message) {
        Matcher matcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);

        if (matcher.find()) {
            try {
                this.amount = Integer.parseInt(matcher.group("moneyAmount"));
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        }

        return false;
    }
}