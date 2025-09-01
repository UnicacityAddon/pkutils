package de.rettichlp.pkutils.command;

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

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

@PKUtilsCommand(label = "reichensteuer")
@PKUtilsListener
public class RichTaxesCommand extends CommandBase implements IMessageReceiveListener {

    private static final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^Ihr Bankguthaben beträgt: (?<moneyBankAmount>([+-])\\d+)\\$$");
    private static final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");
    private static final int RICH_TAXES_THRESHOLD = 100000;

    private static int moneyBankAmount = 0;
    private static int moneyAtmAmount = 0;

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    // execute command to check money on the bank of player
                    networkHandler.sendChatCommand("bank info");

                    // execute command to check money in atm
                    delayedAction(() -> networkHandler.sendChatCommand("atminfo"), 1000);

                    // handle money withdraw
                    delayedAction(() -> {
                        // check atm has money
                        if (moneyAtmAmount <= 0) {
                            sendModMessage("Der ATM hat kein Geld.", false);
                            return;
                        }

                        // check player has rich taxes
                        if (moneyBankAmount <= RICH_TAXES_THRESHOLD) {
                            sendModMessage("Du hast nicht ausreichend Geld auf der Bank.", false);
                            return;
                        }

                        int moneyThatNeedsToBeWithdrawn = moneyBankAmount - RICH_TAXES_THRESHOLD;

                        if (moneyAtmAmount >= moneyThatNeedsToBeWithdrawn) {
                            networkHandler.sendChatCommand("bank abbuchen " + moneyThatNeedsToBeWithdrawn);
                        } else {
                            networkHandler.sendChatCommand("bank abbuchen " + moneyAtmAmount);
                            sendModMessage("Du musst noch " + (moneyThatNeedsToBeWithdrawn - moneyAtmAmount) + "$ abbuchen.", false);
                        }
                    }, 2000);

                    return 1;
                });
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher playerMoneyBankAmountMatcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);
        if (playerMoneyBankAmountMatcher.find()) {
            moneyBankAmount = parseInt(playerMoneyBankAmountMatcher.group("moneyBankAmount"));
            return true;
        }

        Matcher moneyAtmAmountMatcher = MONEY_ATM_AMOUNT.matcher(message);
        if (moneyAtmAmountMatcher.find()) {
            moneyAtmAmount = parseInt(moneyAtmAmountMatcher.group("moneyAtmAmount"));
            return true;
        }

        return true;
    }
}
