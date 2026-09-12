package de.rettichlp.ucutils.listener.impl;

import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.messageService;
import static de.rettichlp.ucutils.UCUtils.notificationService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.synchronisedMinuteTimer;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.UNDERLINE;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.GOLD;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class EconomyListener implements IMessageReceiveListener {

    // bank
    private static final Pattern BANK_STATEMENT_PATTERN = compile("^Ihr Bankguthaben beträgt: \\+(?<amount>\\d+)\\$$");
    private static final Pattern BANK_NEW_BALANCE_PAYDAY_PATTERN = compile("^Neuer Betrag: (?<amount>\\d+)\\$ \\([+-]\\d+\\$\\)$");
    private static final Pattern BANK_TRANSFER_TO_PATTERN = compile("^Du hast (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) (?<amount>\\d+)\\$ überwiesen!$");
    private static final Pattern BANK_TRANSFER_GET_PATTERN = compile("^(?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat dir (?<amount>\\d+)\\$ überwiesen!$");
    private static final Pattern BANK_TRANSFER_FBANK_PATTERN = compile("^Du hast der Fraktion (?<faction>.+) (?<amount>\\d+)\\$ überwiesen!$");
    private static final Pattern BANK_NEW_BALANCE_BANK_PATTERN = compile("^Neuer Bankkontostand: (?<amount>\\d+)\\$$");
    private static final Pattern BANK_NEW_BALANCE_CASH_PATTERN = compile("^Neuer Bargeldbestand: (?<amount>\\d+)\\$$");
    private static final Pattern BANK_DEPOSIT_ATM_TOO_MUCH_PATTERN = compile("^Du versuchst (?<amount>\\d+)\\$ einzuzahlen, der Bankautomat hat aber nur Platz für (?<availableAmount>\\d+)\\$\\. Fortfahren\\? \\[Bestätigen]$");
    private static final Pattern BANK_DAILY_REWARD_PATTERN = compile("^• \\+ (?<amount>\\d+)\\$ \\(auf die Bank\\)$");

    // cash
    private static final Pattern CASH_GIVE_PATTERN = compile("^Du hast (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) (?<amount>\\d+)\\$ gegeben!$");
    private static final Pattern CASH_TAKE_PATTERN = compile("^(?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat dir (?<amount>\\d+)\\$ gegeben!$");
    private static final Pattern CASH_TO_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?<amount>\\d+)\\$ in die Fraktionsbank eingezahlt\\. Grund: (?<reason>.+)$");
    private static final Pattern CASH_FROM_FBANK_PATTERN = compile("^\\[F-Bank] (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?<amount>\\d+)\\$ aus der Fraktionsbank ausgezahlt\\.$");
    private static final Pattern CASH_TO_BANK_PATTERN = compile("^Eingezahlt: \\+(?<amount>\\d+)\\$$");
    private static final Pattern CASH_FROM_BANK_PATTERN = compile("^Auszahlung: -(?<amount>\\d+)\\$$");
    private static final Pattern CASH_GET_PATTERN = compile("^\\+(?<amount>\\d+)\\$$");
    private static final Pattern CASH_GET_COMBO_PATTERN = compile("^\\[Combo] x\\d+ Fang-Combo! \\+(?<amount>\\d+)\\$$");
    private static final Pattern CASH_REMOVE_PATTERN = compile("^-(?<amount>\\d+)\\$$");
    private static final Pattern CASH_STATS_PATTERN = compile("^- Geld: (?<amount>\\d+)\\$$");
    private static final Pattern CASH_DAILY_REWARD_PATTERN = compile("^• \\+ (?<amount>\\d+)\\$ \\(bar\\)$");

    // payday
    private static final Pattern PAYDAY_TIME_PATTERN = compile("^- Zeit seit PayDay: (?<minutes>\\d+)/60 Minuten$");
    private static final Pattern PAYDAY_SALARY_PATTERN = compile("^\\[PayDay] Du bekommst dein Gehalt von (?<money>\\d+)\\$ am PayDay ausgezahlt\\.$");
    private static final Pattern PAYDAY_MINE_SALARY_PATTERN = compile("^\\[PayDay] Du bekommst deine Mine Einnahmen von (?<money>\\d+)\\$ am PayDay ausgezahlt\\.$");
    private static final Pattern PAYDAY_COUNTDOWN_PATTERN = compile("^Info: Du hast in (?<minutes>\\d+) Minuten? deinen PayDay$");

    // stock market
    private static final Pattern STOCK_MARKET_BUY_PATTERN = compile("^\\[Aktien] Du hast (?<amount>\\d+)x (?<company>.+) für (?<price>\\d+)\\$ gekauft\\. \\(Gebühr: (?<fee>\\d+)\\$\\)$");
    private static final Pattern STOCK_MARKET_SELL_PATTERN = compile("^\\[Aktien] (?<amount>\\d+)x (?<company>.+) verkauft für (?<price>\\d+)\\$\\. \\(Gebühr: (?<fee>\\d+)\\$(, Steuer: (?<tax>\\d+)\\$)?\\) (?<brutto>[+-]\\d+)\\$ Brutto / (?<netto>[+-]\\d+)\\$ Netto$");

    // other
    private static final Pattern ATM_MONEY_AMOUNT_PATTERN = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)\\$/100000\\$");
    private static final Pattern BUSINESS_CASH_PATTERN = compile("^Kasse: (\\d+)\\$$");
    private static final Pattern EXP_PATTERN = compile("^(?<amount>[+-]\\d+) Exp!( \\(x(?<multiplier>\\d)\\))?$");
    private static final Pattern MAX_EXP_REACHED_PATTERN = compile("^Du hast die maximale Exp erreicht! Benutze /buylevel um ein Level aufzusteigen\\.$");
    private static final Pattern LOTTO_WIN_PATTERN = compile("^\\[Lotto] Du hast im Lotto gewonnen! \\((?<amount>\\d+)\\$\\)$");
    private static final Pattern BATTLEPASS_REWARD_PATTERN = compile("\\[.+ Pass] \\+(?<amount>\\d+)\\$ erhalten\\.$");
    private static final Pattern MEDIC_DESPAWNED_PATTERN = compile("^Verdammt\\.\\.\\. mein Kopf dröhnt so\\.\\.\\.$");
    private static final Pattern MEDIC_REVIVE_PATTERN = compile("^Du wirst von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) wiederbelebt\\.$");
    private static final Pattern REVIVE_ADMIN_PATTERN = compile("^Du wurdest von \\[UC](?<playerName>[a-zA-Z0-9_]+) wiederbelebt\\.$");
    private static final Pattern BACK_IN_LIFE_PATTERN = compile("^\\[Friedhof] Du lebst nun wieder\\.$");
    private static final Pattern LUMBERJACK_SELL_PATTERN = compile("^\\[Holzfäller] Du hast (\\d+x .+|dein ganzes Inventar abgeladen: \\d+ Items|dein Inventar und Rucksack abgeladen: \\d+ Items) für §6(?<amount>\\d+)\\$§a( §6\\(\\+\\d+% Bonus\\))?( verkauft)?\\.$");

    private long lastMedicReviveAction = 0;
    private boolean maxExperiencePerLevelReached = false;

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher bankStatementMatcher = BANK_STATEMENT_PATTERN.matcher(message);
        if (bankStatementMatcher.find()) {
            int amount = parseInt(bankStatementMatcher.group("amount"));
            configuration.setMoneyBankAmount(amount);

            List<String> commands = new ArrayList<>();
            commands.add("atminfo");

            switch (configuration.getOptions().miscellaneous().atmInformationType()) {
                case NONE -> {
                }
                case F_BANK -> commands.add("fbank");
                case G_BANK -> commands.add("gruppierungkasse");
                case BOTH -> commands.addAll(List.of("fbank", "gruppierungkasse"));
            }

            commandService.sendCommands(commands);

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

        Matcher bankTransferFbankMatcher = BANK_TRANSFER_FBANK_PATTERN.matcher(message);
        if (bankTransferFbankMatcher.find()) {
            int amount = parseInt(bankTransferFbankMatcher.group("amount"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() - amount);
            return true;
        }

        Matcher bankNewBalanceBankMatcher = BANK_NEW_BALANCE_BANK_PATTERN.matcher(message);
        if (bankNewBalanceBankMatcher.find()) {
            int amount = parseInt(bankNewBalanceBankMatcher.group("amount"));
            configuration.setMoneyBankAmount(amount);
            return true;
        }

        Matcher bankNewBalanceCashMatcher = BANK_NEW_BALANCE_CASH_PATTERN.matcher(message);
        if (bankNewBalanceCashMatcher.find()) {
            int amount = parseInt(bankNewBalanceCashMatcher.group("amount"));
            configuration.setMoneyCashAmount(amount);
            return true;
        }

        Matcher bankDepositAtmTooMuchMatcher = BANK_DEPOSIT_ATM_TOO_MUCH_PATTERN.matcher(message);
        if (bankDepositAtmTooMuchMatcher.find()) {
            int availableAmount = parseInt(bankDepositAtmTooMuchMatcher.group("availableAmount"));

            MutableComponent modified = text.copy()
                    .append(SPACE)
                    .append(literal("[" + availableAmount + "$ einzahlen]").withStyle(style -> style
                            .withColor(GOLD)
                            .withClickEvent(new ClickEvent.RunCommand("/bank einzahlen " + availableAmount))
                            .withHoverEvent(new HoverEvent.ShowText(literal("Nur " + availableAmount + "$ einzahlen").withColor(GOLD)))));

            player.sendSystemMessage(modified);

            return false;
        }

        Matcher bankDailyRewardMatcher = BANK_DAILY_REWARD_PATTERN.matcher(message);
        if (bankDailyRewardMatcher.find()) {
            int amount = parseInt(bankDailyRewardMatcher.group("amount"));
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

            if (playerName.equals(player.getGameProfile().name())) {
                int amount = parseInt(cashToFbankMatcher.group("amount"));
                configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - amount);
            }

            return true;
        }

        Matcher cashFromFbankMatcher = CASH_FROM_FBANK_PATTERN.matcher(message);
        if (cashFromFbankMatcher.find()) {
            String playerName = cashFromFbankMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().name())) {
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

        Matcher cashGetComboMatcher = CASH_GET_COMBO_PATTERN.matcher(message);
        if (cashGetComboMatcher.find()) {
            int amount = parseInt(cashGetComboMatcher.group("amount"));
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

        Matcher cashDailyRewardMatcher = CASH_DAILY_REWARD_PATTERN.matcher(message);
        if (cashDailyRewardMatcher.find()) {
            int amount = parseInt(cashDailyRewardMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
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

        Matcher paydayMineSalaryMatcher = PAYDAY_MINE_SALARY_PATTERN.matcher(message);
        if (paydayMineSalaryMatcher.find()) {
            int money = parseInt(paydayMineSalaryMatcher.group("money"));
            configuration.addPredictedPayDaySalary(money);
            return true;
        }

        Matcher paydayCountdownMatcher = PAYDAY_COUNTDOWN_PATTERN.matcher(message);
        if (paydayCountdownMatcher.find()) {
            int minutes = parseInt(paydayCountdownMatcher.group("minutes"));
            int minutesSinceLastPayDay = 60 - minutes;
            synchronisedMinuteTimer.synchronize();
            configuration.setMinutesSinceLastPayDay(minutesSinceLastPayDay);

            utilService.delayedAction(() -> {
                if (configuration.getMoneyBankAmount() > 100000) {
                    messageService.sendModMessage("Du hast über 100000$ auf der Bank!", false);

                    switch (minutes) {
                        case 10 -> notificationService.notificationSound(1);
                        case 5 -> notificationService.notificationSound(2);
                        case 3, 2, 1 -> notificationService.notificationSound(3);
                    }
                }
            }, 50);

            return true;
        }

        Matcher stockMarketBuyMatcher = STOCK_MARKET_BUY_PATTERN.matcher(message);
        if (stockMarketBuyMatcher.find()) {
            int price = parseInt(stockMarketBuyMatcher.group("price"));
            int fee = parseInt(stockMarketBuyMatcher.group("fee"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() - price - fee);
            return true;
        }

        Matcher stockMarketSellMatcher = STOCK_MARKET_SELL_PATTERN.matcher(message);
        if (stockMarketSellMatcher.find()) {
            int price = parseInt(stockMarketSellMatcher.group("price"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + price);
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

            MutableComponent appendedText = text.copy().append(" ")
                    .append(literal("Geld entnehmen").withColor(GRAY).withStyle(UNDERLINE))
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent.RunCommand("/biz kasse get " + amountString))
                            .withHoverEvent(new HoverEvent.ShowText(literal("Klicke, um " + amountString + "$ aus der Kasse zu nehmen.")))
                    );

            player.sendSystemMessage(appendedText);
            return false;
        }

        Matcher expMatcher = EXP_PATTERN.matcher(message);
        if (expMatcher.find()) {
            int amount = parseInt(expMatcher.group("amount"));
            String multiplierString = expMatcher.group("multiplier");
            int multiplier = ofNullable(multiplierString).map(Integer::parseInt).orElse(1);

            configuration.addPredictedPayDayExp(amount * multiplier);

            if (this.maxExperiencePerLevelReached) {
                MutableComponent modifiedMessage = text.copy()
                        .append(SPACE)
                        .append(literal("↑").withStyle(style -> style
                                .withHoverEvent(new HoverEvent.ShowText(literal("Du hast die maximale Exp erreicht! Benutze /buylevel um ein Level aufzusteigen.").withColor(RED)))
                                .withColor(RED)
                                .withBold(true)));
                player.sendSystemMessage(modifiedMessage);
                return false;
            }

            return true;
        }

        Matcher maxExpReachedMatcher = MAX_EXP_REACHED_PATTERN.matcher(message);
        if (maxExpReachedMatcher.find()) {
            boolean sendNotification = !this.maxExperiencePerLevelReached;
            this.maxExperiencePerLevelReached = true;
            return sendNotification; // notify only first time, after that hide message
        }

        Matcher lottoWinMatcher = LOTTO_WIN_PATTERN.matcher(message);
        if (lottoWinMatcher.find()) {
            int amount = parseInt(lottoWinMatcher.group("amount"));
            configuration.setMoneyBankAmount(configuration.getMoneyBankAmount() + amount);
            return true;
        }

        Matcher battlepassRewardMatcher = BATTLEPASS_REWARD_PATTERN.matcher(message);
        if (battlepassRewardMatcher.find()) {
            int amount = parseInt(battlepassRewardMatcher.group("amount"));
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
            this.lastMedicReviveAction = currentTimeMillis();
            return true;
        }

        Matcher backInLifeMatcher = BACK_IN_LIFE_PATTERN.matcher(message);
        if (backInLifeMatcher.find()) {
            long timeSingeLastMedicReviveAction = currentTimeMillis() - this.lastMedicReviveAction;
            if (timeSingeLastMedicReviveAction > 6000 && timeSingeLastMedicReviveAction < 10000) {
                configuration.setMoneyBankAmount(max(0, configuration.getMoneyBankAmount() - 50));
            }

            storage.setDead(false);
            return true;
        }

        Matcher lumberjackSellMatcher = LUMBERJACK_SELL_PATTERN.matcher(message);
        if (lumberjackSellMatcher.find()) {
            int amount = parseInt(lumberjackSellMatcher.group("amount"));
            configuration.setMoneyCashAmount(configuration.getMoneyCashAmount() + amount);
        }

        return true;
    }
}
