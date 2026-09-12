package de.rettichlp.ucutils.listener.impl;

import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.common.services.MessageService.DATE_TIME_FORMAT;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.util.regex.Pattern.compile;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.DARK_RED;
import static net.minecraft.network.chat.TextColor.GOLD;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.GREEN;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class HouseListener implements IMessageReceiveListener {

    private static final Pattern HOUSE_RENTER_HEADER_PATTERN = compile("^=== Mieter in Haus (?<number>\\d+) ===$");
    private static final Pattern HOUSE_RENTER_ENTRY_ONLINE_PATTERN = compile("^» (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) \\(Online\\)$");
    private static final Pattern HOUSE_RENTER_ENTRY_OFFLINE_PATTERN = compile("^» (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) \\(Offline seit (?<dateTime>\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2})\\)$");

    private int lastHouseNumber = 0;

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher houseRenterHeaderMatcher = HOUSE_RENTER_HEADER_PATTERN.matcher(message);
        if (houseRenterHeaderMatcher.find()) {
            this.lastHouseNumber = parseInt(houseRenterHeaderMatcher.group("number"));
            return true;
        }

        Matcher houseRenterEntryOnlineMatcher = HOUSE_RENTER_ENTRY_ONLINE_PATTERN.matcher(message);
        if (houseRenterEntryOnlineMatcher.find() && this.lastHouseNumber != 0) {
            String playerName = houseRenterEntryOnlineMatcher.group("playerName");

            MutableComponent modifiedText = empty()
                    .append(literal("  » ").withColor(GRAY))
                    .append(literal(playerName).withColor(GOLD))
                    .append(literal(" (").withColor(DARK_GRAY))
                    .append(literal("Online").withColor(GREEN))
                    .append(literal(") ").withColor(DARK_GRAY))
                    .append(literal("⨉").withStyle(style -> style
                            .withColor(RED)
                            .withBold(true)
                            .withHoverEvent(new HoverEvent.ShowText(literal("Kündigen").withColor(RED)))
                            .withClickEvent(new ClickEvent.RunCommand("/unrent " + this.lastHouseNumber + " " + playerName))));

            player.sendSystemMessage(modifiedText);
            return false;
        }

        Matcher houseRenterEntryOfflineMatcher = HOUSE_RENTER_ENTRY_OFFLINE_PATTERN.matcher(message);
        if (houseRenterEntryOfflineMatcher.find() && this.lastHouseNumber != 0) {
            String playerName = houseRenterEntryOfflineMatcher.group("playerName");
            String dateTimeString = houseRenterEntryOfflineMatcher.group("dateTime");
            LocalDateTime dateTime = parse(dateTimeString, DATE_TIME_FORMAT);
            long daysSinceOffline = abs(between(now(), dateTime).toDays());

            MutableComponent modifiedText = empty()
                    .append(literal("  » ").withColor(GRAY))
                    .append(literal(playerName).withColor(GOLD))
                    .append(literal(" (").withColor(DARK_GRAY))
                    .append(literal("Offline seit " + dateTimeString).withColor(RED))
                    .append(literal(" - ").withColor(DARK_GRAY))
                    .append(literal(daysSinceOffline + " " + (daysSinceOffline == 1 ? "Tag" : "Tage")).withColor(DARK_RED))
                    .append(literal(") ").withColor(DARK_GRAY))
                    .append(literal("⨉").withStyle(style -> style
                            .withColor(RED)
                            .withBold(true)
                            .withHoverEvent(new HoverEvent.ShowText(literal("Kündigen").withColor(RED)))
                            .withClickEvent(new ClickEvent.RunCommand("/unrent " + this.lastHouseNumber + " " + playerName))));

            player.sendSystemMessage(modifiedText);
            return false;
        }

        return true;
    }
}
