package de.rettichlp.pkutils.listener.impl.business;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.player;

@PKUtilsListener
public class BusinessListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern KASSE_PATTERN = Pattern.compile("^Kasse: (\\d+)\\$$");

    @Override
    public boolean onMessageReceive(String message) {
        Matcher kasseMatcher = KASSE_PATTERN.matcher(message);
        if (kasseMatcher.find()) {
            String amount = kasseMatcher.group(1);
            int amountInt = Integer.parseInt(amount);

            // Nur klickbar machen, wenn Geld in der Kasse ist
            if (amountInt > 0) {
                MutableText newText = Text.empty()
                        .append(Text.of("Kasse: " + amount + "$").copy().formatted(Formatting.GOLD))
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/biz kasse get " + amount))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("§7Klicke, um §b" + amount + "$§7 aus der Kasse zu nehmen.")))
                        );

                player.sendMessage(newText, false);
                return false;
            }
        }
        return true;
    }
}