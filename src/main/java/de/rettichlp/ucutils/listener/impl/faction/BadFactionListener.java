package de.rettichlp.ucutils.listener.impl.faction;

import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.DARK_RED;

@UCUtilsListener
public class BadFactionListener implements IMessageReceiveListener {

    private static final Pattern BLACK_MARKET_DEALER_ENTRY_PATTERN = compile("^» (?<location>.+) – (gerade eben|vor \\d+ \\w+) {2}\\[Navi]$");

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher blackMarketDealerEntryMatcher = BLACK_MARKET_DEALER_ENTRY_PATTERN.matcher(message);
        if (blackMarketDealerEntryMatcher.find()) {
            Vec3 blackMarketPosition = storage.getBlackMarketPosition();
            Vec3 dealerPosition = storage.getDealerPosition();

            // extract location from message
            Component last = text.getSiblings().getLast();
            ClickEvent clickEvent = last.getStyle().getClickEvent();
            if (!(clickEvent instanceof ClickEvent.RunCommand(String command))) {
                return true;
            }

            String[] locationPieces = command.replace("/navi ", "").split("/");

            if (locationPieces.length != 3) {
                return true;
            }

            Vec3 extractedPosition = new Vec3(parseInt(locationPieces[0]), parseInt(locationPieces[1]), parseInt(locationPieces[2]));

            if ((blackMarketPosition != null && extractedPosition.distanceToSqr(blackMarketPosition) < 5) || (dealerPosition != null && extractedPosition.distanceToSqr(dealerPosition) < 5)) {
                MutableComponent text1 = text.copy().append(literal(" ⯐").withColor(DARK_RED).withStyle(BOLD));
                player.sendSystemMessage(text1);
                return false;
            }

            return true;
        }

        return true;
    }
}
