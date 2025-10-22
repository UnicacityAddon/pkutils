package de.rettichlp.pkutils.command.money;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

@PKUtilsCommand(label = "einzahlen")
@PKUtilsListener
public class DepositCommand extends CommandBase implements IMessageReceiveListener {

    private static final Pattern PLAYER_MONEY_AMOUNT_PATTERN = compile("- Geld: (?<moneyAmount>\\d+)\\$");

    private static int amount = 0;

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    commandService.sendCommand("stats");

                    utilService.delayedAction(() -> {
                        if (amount <= 0) {
                            messageService.sendModMessage("Du hast kein Geld zum Einzahlen.", false);
                        } else {
                            commandService.sendCommand("bank einzahlen " + amount);
                        }
                    }, 1000);

                    return 1;
                });
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher playerMoneyAmountMatcher = PLAYER_MONEY_AMOUNT_PATTERN.matcher(message);
        if (playerMoneyAmountMatcher.find()) {
            amount = parseInt(playerMoneyAmountMatcher.group("moneyAmount"));
        }

        return true;
    }
}
