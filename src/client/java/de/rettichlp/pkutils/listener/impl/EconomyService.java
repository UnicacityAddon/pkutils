package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.UNDERLINE;

@PKUtilsListener
public class EconomyService extends PKUtilsBase implements IMessageReceiveListener {

    // payday
    private static final Pattern PAYDAY_PATTERN = compile("^======== PayDay ========$");
    private static final Pattern PAYDAY_TIME_PATTERN = compile("^ - Zeit seit Payday: (?<minutes>\\d+)/60 Minuten$");
    private static final Pattern PAYDAY_SALARY_PATTERN = compile("^\\[PayDay] Du bekommst dein Gehalt von (?<money>\\d+)\\$ am PayDay ausgezahlt\\.$");

    private static final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^Ihr Bankguthaben beträgt: (?<moneyBankAmount>([+-])\\d+)\\$$");
    private static final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");
    private static final Pattern BUSINESS_CASH_PATTERN = compile("^Kasse: (\\d+)\\$$");
    private static final Pattern EXP_PATTERN = compile("(?<amount>[+-]\\d+) Exp!( \\(x(?<multiplier>\\d)\\))?");

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher paydayMatcher = PAYDAY_PATTERN.matcher(message);
        if (paydayMatcher.find()) {
            configService.edit(mainConfig -> {
                mainConfig.setMinutesSinceLastPayDay(0);
                mainConfig.setPredictedPayDaySalary(0);
                mainConfig.setPredictedPayDayExp(0);
            });
        }

        Matcher paydayTimeMatcher = PAYDAY_TIME_PATTERN.matcher(message);
        if (paydayTimeMatcher.find()) {
            int minutesSinceLastPayDay = parseInt(paydayTimeMatcher.group("minutes"));
            configService.edit(mainConfig -> mainConfig.setMinutesSinceLastPayDay(minutesSinceLastPayDay));
            return true;
        }

        Matcher paydaySalaryMatcher = PAYDAY_SALARY_PATTERN.matcher(message);
        if (paydaySalaryMatcher.find()) {
            int money = parseInt(paydaySalaryMatcher.group("money"));
            configService.edit(mainConfig -> mainConfig.addPredictedPayDaySalary(money));
            storage.setCurrentJob(null);
            return true;
        }

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

        Matcher expMatcher = EXP_PATTERN.matcher(message);
        if (expMatcher.find()) {
            int amount = parseInt(expMatcher.group("amount"));
            int multiplier = expMatcher.namedGroups().containsKey("multiplier") ? parseInt(expMatcher.group("multiplier")) : 1;

            configService.edit(mainConfig -> mainConfig.addPredictedPayDayExp(amount * multiplier));
            return true;
        }

        Matcher businessCashMatcher = BUSINESS_CASH_PATTERN.matcher(message);
        if (businessCashMatcher.find()) {
            String amountString = businessCashMatcher.group(1);

            MutableText appendedText = text.copy().append(" ")
                    .append(of("Geld entnehmen").copy().formatted(GRAY, UNDERLINE))
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(RUN_COMMAND, "/biz kasse get " + amountString))
                            .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Klicke, um " + amountString + "$ aus der Kasse zu nehmen.")))
                    );

            player.sendMessage(appendedText, false);
            return false;
        }

        return true;
    }
}
