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

import static de.rettichlp.pkutils.PKUtilsClient.*;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
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
    private static final Pattern PLAYER_MONEY_CASH_AMOUNT = compile("^ - Geld: (?<moneyCashAmount>\\d+)\\$$");
    private static final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");
    private static final Pattern BUSINESS_CASH_PATTERN = compile("^Kasse: (\\d+)\\$$");
    private static final Pattern EXP_PATTERN = compile("(?<amount>[+-]\\d+) Exp!( \\(x(?<multiplier>\\d)\\))?");

    private static final Pattern BANK_TRANSFER_TO_PATTERN = compile("^Du hast (?:\\[PK])?(?<player>\\w+) (?<money>([+])\\d+)\\$ überwiesen.$");
    private static final Pattern BANK_TRANSFER_GET_PATTERN = compile("^(?:\\[PK])?(?<player>\\w+) hat dir (?<money>([+])\\d+)\\$ überwiesen.$");
    private static final Pattern BANK_PAYDAY_PATTERN = Pattern.compile("^Neuer Betrag: (?<money>\\d+)\\$ \\([+-]\\d+\\$\\)$");
    private static final Pattern CASH_GIVE_PATTERN = compile("^\\Du hast (?:\\[PK])?(?<player>\\w+) (?<money>([+-])\\d+)\\$ gegeben.$");
    private static final Pattern CASH_GET_PATTERN = compile("^(?:\\[PK])?(?<player>\\w+) hat dir (?<money>([+-])\\d+)\\$ gegeben.$");
    private static final Pattern CASH_TO_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[PK])*(?<player>\\w+) hat (?<money>([+])\\d+)\\$ in die F-Bank eingezahlt\\.$");
    private static final Pattern CASH_FROM_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[PK])*(?<player>\\w+) hat (?<money>([+])\\d+)\\$ aus der F-Bank genommen\\.$");
    private static final Pattern CASH_TO_BANK_PATTERN = compile("^Einzahlung: (?<money>([+])\\d+)\\$$");
    private static final Pattern CASH_FROM_BANK_PATTERN = compile("^Auszahlung: -(?<money>\\d+)\\$$");
    private static final Pattern CASH_ADD_PATTERN = compile("^(?<money>([+-])\\d+)\\$$");
    private static final Pattern CASH_REMOVE_PATTERN = compile("^(?<money>([+-])\\d+)\\$$");
    private static final Pattern LOTTO_WIN = Pattern.compile("^\\[Lotto] Du hast im Lotto gewonnen! \\((?<money>\\d+)\\$\\)$"); //TODO: fix matcher // Weiß nicht, ob es auf die Hand oder Bank geht.
    //TODO: VOTE REWARD PATTERN

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher paydayMatcher = PAYDAY_PATTERN.matcher(message);
        if (paydayMatcher.find()) {
            configuration.setMinutesSinceLastPayDay(0);
            configuration.setPredictedPayDaySalary(0);
            configuration.setPredictedPayDayExp(0);
        }

        Matcher cashAddMatcher = CASH_ADD_PATTERN.matcher(message);
        if (cashAddMatcher.find()) {
            int money = parseInt(cashAddMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + money);
            return true;
        }
        Matcher cashRemoveMatcher = CASH_REMOVE_PATTERN.matcher(message);
        if (cashRemoveMatcher.find()) {
            int money = parseInt(cashRemoveMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - money);
            return true;
        }
        Matcher lottoWinMatcher = LOTTO_WIN.matcher(message);
        if (lottoWinMatcher.find()) {
            int money = parseInt(lottoWinMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + money);
            return true;
        }
        Matcher bankPaydayMatcher = BANK_PAYDAY_PATTERN.matcher(message);
        if (bankPaydayMatcher.find()) {
            int money = parseInt(bankPaydayMatcher.group("money"));
            configuration.setMoneyBankAmount(money);
            return true;
        }
        Matcher bankTransferToMatcher = BANK_TRANSFER_TO_PATTERN.matcher(message);
        if (bankTransferToMatcher.find()) {
            int money = parseInt(bankTransferToMatcher.group("money"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() - money);
            return true;
        }
        Matcher bankTransferGetMatcher = BANK_TRANSFER_GET_PATTERN.matcher(message);
        if (bankTransferGetMatcher.find()) {
            int money = parseInt(bankTransferGetMatcher.group("money"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + money);
            return true;
        }
        Matcher cashGiveMatcher = CASH_GIVE_PATTERN.matcher(message);
        if (cashGiveMatcher.find()) {
            int money = parseInt(cashGiveMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - money);
            return true;
        }
        Matcher cashGetMatcher = CASH_GET_PATTERN.matcher(message);
        if (cashGetMatcher.find()) {
            int money = parseInt(cashGetMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + money);
            return true;
        }
        Matcher cashToFbankMatcher = CASH_TO_FBANK_PATTERN.matcher(message);
        if (cashToFbankMatcher.find()) {
            int money = parseInt(cashToFbankMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - money);
            return true;
        }
        Matcher cashFromFbankMatcher = CASH_FROM_FBANK_PATTERN.matcher(message);
        if (cashFromFbankMatcher.find()) {
            int money = parseInt(cashFromFbankMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + money);
            return true;
        }
        Matcher cashToBankMatcher = CASH_TO_BANK_PATTERN.matcher(message);
        if (cashToBankMatcher.find()) {
            int money = parseInt(cashToBankMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - money);
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + money);
            return true;
        }
        Matcher cashFromBankMatcher = CASH_FROM_BANK_PATTERN.matcher(message);
        if (cashFromBankMatcher.find()) {
            int money = parseInt(cashFromBankMatcher.group("money"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + money);
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() - money);
            return true;
        }


        Matcher paydayTimeMatcher = PAYDAY_TIME_PATTERN.matcher(message);
        if (paydayTimeMatcher.find()) {
            int minutesSinceLastPayDay = parseInt(paydayTimeMatcher.group("minutes"));
            configuration.setMinutesSinceLastPayDay(minutesSinceLastPayDay);
            return true;
        }

        Matcher paydaySalaryMatcher = PAYDAY_SALARY_PATTERN.matcher(message);
        if (paydaySalaryMatcher.find()) {
            int money = parseInt(paydaySalaryMatcher.group("money"));
            configuration.addPredictedPayDaySalary(money);
            storage.setCurrentJob(null);
            return true;
        }

        Matcher playerMoneyBankAmountMatcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);
        if (playerMoneyBankAmountMatcher.find()) {
            int moneyBankAmount = parseInt(playerMoneyBankAmountMatcher.group("moneyBankAmount"));
            configuration.setMoneyBankAmount(moneyBankAmount);
            return true;
        }

        Matcher playerMoneyCashAmountMatcher = PLAYER_MONEY_CASH_AMOUNT.matcher(message);
        if (playerMoneyCashAmountMatcher.find()) {
            int moneyCashAmount = parseInt(playerMoneyCashAmountMatcher.group("moneyCashAmount"));
            configuration.setMoneyCashAmount(moneyCashAmount);
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
            String multiplierString = expMatcher.group("multiplier");
            int multiplier = ofNullable(multiplierString).map(Integer::parseInt).orElse(1);

            configuration.addPredictedPayDayExp(amount * multiplier);
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
