package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class EconomyService extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^Ihr Bankguthaben beträgt: (?<moneyBankAmount>([+-])\\d+)\\$$");
    private static final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher playerMoneyBankAmountMatcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);
        if (playerMoneyBankAmountMatcher.find()) {
            int moneyBankAmount = parseInt(playerMoneyBankAmountMatcher.group("moneyBankAmount"));
            storage.setMoneyBankAmount(moneyBankAmount);
            return true;
        }

        Matcher moneyAtmAmountMatcher = MONEY_ATM_AMOUNT.matcher(message);
        if (moneyAtmAmountMatcher.find()) {
            int moneyAtmAmount = parseInt(moneyAtmAmountMatcher.group("moneyAtmAmount"));
            storage.setMoneyAtmAmount(moneyAtmAmount);
            return true;
        }

        return true;
    }
}
