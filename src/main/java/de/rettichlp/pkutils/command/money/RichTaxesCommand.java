package de.rettichlp.pkutils.command.money;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilService;

@PKUtilsCommand(label = "reichensteuer")
public class RichTaxesCommand extends CommandBase {

    private static final int RICH_TAXES_THRESHOLD = 100000;

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    // execute command to check money on the bank of player
                    commandService.sendCommand("bank info");

                    // execute command to check money in atm
                    utilService.delayedAction(() -> commandService.sendCommand("atminfo"), 1000);

                    // handle money withdraw
                    utilService.delayedAction(() -> {
                        int moneyAtmAmount = storage.getMoneyAtmAmount();
                        int moneyBankAmount = configuration.getMoneyBankAmount();

                        // check atm has money
                        if (moneyAtmAmount <= 0) {
                            messageService.sendModMessage("Der ATM hat kein Geld.", false);
                            return;
                        }

                        // check player has rich taxes
                        if (moneyBankAmount <= RICH_TAXES_THRESHOLD) {
                            messageService.sendModMessage("Du hast nicht ausreichend Geld auf der Bank.", false);
                            return;
                        }

                        int moneyThatNeedsToBeWithdrawn = moneyBankAmount - RICH_TAXES_THRESHOLD;

                        if (moneyAtmAmount >= moneyThatNeedsToBeWithdrawn) {
                            commandService.sendCommand("bank abbuchen " + moneyThatNeedsToBeWithdrawn);
                        } else {
                            commandService.sendCommand("bank abbuchen " + moneyAtmAmount);
                            messageService.sendModMessage("Du musst noch " + (moneyThatNeedsToBeWithdrawn - moneyAtmAmount) + "$ abbuchen.", false);
                        }
                    }, 2000);

                    return 1;
                });
    }
}
