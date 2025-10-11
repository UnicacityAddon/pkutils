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

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.UNDERLINE;

@PKUtilsListener
public class EconomyService extends PKUtilsBase implements IMessageReceiveListener {

    // bank
    private static final Pattern BANK_STATEMENT_PATTERN = compile("^Ihr Bankguthaben beträgt: \\+(?<amount>\\d+)\\$$");
    private static final Pattern BANK_NEW_BALANCE_PAYDAY_PATTERN = compile("^Neuer Betrag: (?<amount>\\d+)\\$ \\([+-]\\d+\\$\\)$");
    private static final Pattern BANK_TRANSFER_TO_PATTERN = compile("^Du hast (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) (?<amount>\\d+)\\$ überwiesen\\.$");
    private static final Pattern BANK_TRANSFER_GET_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat dir (?<amount>\\d+)\\$ überwiesen\\.$");

    // cash
    private static final Pattern CASH_GIVE_PATTERN = compile("^Du hast (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) (?<amount>\\d+)\\$ gegeben\\.$");
    private static final Pattern CASH_TAKE_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat dir (?<amount>\\d+)\\$ gegeben\\.$");
    private static final Pattern CASH_TO_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat (?<amount>\\d+)\\$ in die F-Bank eingezahlt\\.$");
    private static final Pattern CASH_FROM_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat (?<amount>\\d+)\\$ aus der F-Bank ausgezahlt\\.$");
    private static final Pattern CASH_TO_BANK_PATTERN = compile("^Einzahlung: \\+(?<amount>\\d+)\\$$");
    private static final Pattern CASH_FROM_BANK_PATTERN = compile("^Auszahlung: -(?<amount>\\d+)\\$$");
    private static final Pattern CASH_GET_PATTERN = compile("^\\+(?<amount>\\d+)\\$$");
    private static final Pattern CASH_REMOVE_PATTERN = compile("^-(?<amount>\\d+)\\$$");
    private static final Pattern CASH_STATS_PATTERN = compile("^ - Geld: (?<amount>\\d+)\\$$");

    // payday
    private static final Pattern PAYDAY_TIME_PATTERN = compile("^ - Zeit seit Payday: (?<minutes>\\d+)/60 Minuten$");
    private static final Pattern PAYDAY_SALARY_PATTERN = compile("^\\[PayDay] Du bekommst dein Gehalt von (?<money>\\d+)\\$ am PayDay ausgezahlt\\.$");

    // other
    private static final Pattern ATM_MONEY_AMOUNT_PATTERN = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");
    private static final Pattern BUSINESS_CASH_PATTERN = compile("^Kasse: (\\d+)\\$$");
    private static final Pattern EXP_PATTERN = compile("(?<amount>[+-]\\d+) Exp!( \\(x(?<multiplier>\\d)\\))?");
    private static final Pattern LOTTO_WIN_PATTERN = compile("^\\[Lotto] Du hast im Lotto gewonnen! \\((?<amount>\\d+)\\$\\)$");
    private static final Pattern MEDIC_DESPAWNED_PATTERN = compile("^Verdammt\\.\\.\\. mein Kopf dröhnt so\\.\\.\\.$");
    private static final Pattern MEDIC_REVIVE_PATTERN = compile("^Du wirst von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) wiederbelebt\\.$");

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher bankStatementMatcher = BANK_STATEMENT_PATTERN.matcher(message);
        if (bankStatementMatcher.find()) {
            int amount = parseInt(bankStatementMatcher.group("amount"));
            configuration.setMoneyBankAmount(amount);
            return true;
        }

        Matcher bankNewBalancePaydayMatcher = BANK_NEW_BALANCE_PAYDAY_PATTERN.matcher(message);
        if (bankNewBalancePaydayMatcher.find()) {
            int amount = parseInt(bankNewBalancePaydayMatcher.group("amount"));
            configuration.setMoneyBankAmount(amount);
            configuration.setMinutesSinceLastPayDay(0);
            configuration.setPredictedPayDaySalary(0);
            configuration.setPredictedPayDayExp(0);
            return true;
        }

        Matcher bankTransferToMatcher = BANK_TRANSFER_TO_PATTERN.matcher(message);
        if (bankTransferToMatcher.find()) {
            int amount = parseInt(bankTransferToMatcher.group("amount"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() - amount);
            return true;
        }

        Matcher bankTransferGetMatcher = BANK_TRANSFER_GET_PATTERN.matcher(message);
        if (bankTransferGetMatcher.find()) {
            int amount = parseInt(bankTransferGetMatcher.group("amount"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + amount);
            return true;
        }

        Matcher cashGiveMatcher = CASH_GIVE_PATTERN.matcher(message);
        if (cashGiveMatcher.find()) {
            int amount = parseInt(cashGiveMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - amount);
            return true;
        }

        Matcher cashTakeMatcher = CASH_TAKE_PATTERN.matcher(message);
        if (cashTakeMatcher.find()) {
            int amount = parseInt(cashTakeMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
            return true;
        }

        Matcher cashToFbankMatcher = CASH_TO_FBANK_PATTERN.matcher(message);
        if (cashToFbankMatcher.find()) {
            String playerName = cashToFbankMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().getName())) {
                int amount = parseInt(cashToFbankMatcher.group("amount"));
                configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - amount);
            }

            return true;
        }

        Matcher cashFromFbankMatcher = CASH_FROM_FBANK_PATTERN.matcher(message);
        if (cashFromFbankMatcher.find()) {
            String playerName = cashFromFbankMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().getName())) {
                int amount = parseInt(cashFromFbankMatcher.group("amount"));
                configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
            }

            return true;
        }

        Matcher cashToBankMatcher = CASH_TO_BANK_PATTERN.matcher(message);
        if (cashToBankMatcher.find()) {
            int amount = parseInt(cashToBankMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - amount);
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + amount);
            return true;
        }

        Matcher cashFromBankMatcher = CASH_FROM_BANK_PATTERN.matcher(message);
        if (cashFromBankMatcher.find()) {
            int amount = parseInt(cashFromBankMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() - amount);
            return true;
        }

        Matcher cashGetMatcher = CASH_GET_PATTERN.matcher(message);
        if (cashGetMatcher.find()) {
            int amount = parseInt(cashGetMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
            return true;
        }

        Matcher cashRemoveMatcher = CASH_REMOVE_PATTERN.matcher(message);
        if (cashRemoveMatcher.find()) {
            int amount = parseInt(cashRemoveMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - amount);
            return true;
        }

        Matcher cashStatsMatcher = CASH_STATS_PATTERN.matcher(message);
        if (cashStatsMatcher.find()) {
            int amount = parseInt(cashStatsMatcher.group("amount"));
            configuration.setMoneyCashAmount(amount);
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

        Matcher moneyAtmAmountMatcher = ATM_MONEY_AMOUNT_PATTERN.matcher(message);
        if (moneyAtmAmountMatcher.find()) {
            int moneyAtmAmount = parseInt(moneyAtmAmountMatcher.group("moneyAtmAmount"));
            storage.setMoneyAtmAmount(moneyAtmAmount);
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

        Matcher expMatcher = EXP_PATTERN.matcher(message);
        if (expMatcher.find()) {
            int amount = parseInt(expMatcher.group("amount"));
            String multiplierString = expMatcher.group("multiplier");
            int multiplier = ofNullable(multiplierString).map(Integer::parseInt).orElse(1);

            configuration.addPredictedPayDayExp(amount * multiplier);
            return true;
        }

        Matcher lottoWinMatcher = LOTTO_WIN_PATTERN.matcher(message);
        if (lottoWinMatcher.find()) {
            int amount = parseInt(lottoWinMatcher.group("amount"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + amount);
            return true;
        }

        Matcher medicDespawnedMatcher = MEDIC_DESPAWNED_PATTERN.matcher(message);
        if (medicDespawnedMatcher.find()) {
            configuration.setMoneyCashAmount(0);
            return true;
        }

        Matcher medicReviveMatcher = MEDIC_REVIVE_PATTERN.matcher(message);
        if (medicReviveMatcher.find()) {
            configuration.setMoneyBankAmount(max(0, configuration.getMoneyBankAmount() - 50));
            return true;
        }

        return true;
    }
}
