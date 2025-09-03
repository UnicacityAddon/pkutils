package de.rettichlp.pkutils.listener.impl.business;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.UNDERLINE;

@PKUtilsListener
public class BusinessListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern BUSINESS_CASH_PATTERN = compile("^Kasse: (\\d+)\\$$");

    @Override
    public boolean onMessageReceive(Text text, String message) {
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
