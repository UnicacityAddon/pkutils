package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.common.models.Activity.Type.REVIVE;
import static java.time.Duration.between;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class ReviveListener extends PKUtilsBase implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern KARMA_GET_PATTERN = compile("^ » Karma: (?<amount>[+-]\\d+) Karma \\((?<amountOverall>[+-]\\d+)\\)$");

    private LocalDateTime lastReviveStartetAt = MIN;

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (command.startsWith("revive")) {
            this.lastReviveStartetAt = now();
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher karmaGetMatcher = KARMA_GET_PATTERN.matcher(message);
        if (!karmaGetMatcher.find()) {
            return true;
        }

        long seconds = between(this.lastReviveStartetAt, now()).toSeconds();
        if (seconds > 6 && seconds < 10) {
            api.trackActivity(REVIVE);
        }

        return true;
    }
}
