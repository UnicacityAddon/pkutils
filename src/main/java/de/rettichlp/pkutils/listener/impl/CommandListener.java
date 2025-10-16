package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.CommandResponseRetriever;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.storage;
import static java.lang.Character.isUpperCase;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class CommandListener extends PKUtilsBase implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern COMMAND_NAVI_HOUSE_NUMBER_PATTERN = compile("^navi (?<number>\\d+)$");

    @Override
    public boolean onCommandSend(@NotNull String command) {
        String[] parts = command.split(" ", 2); // split the message into command label and arguments
        String commandLabel = parts[0]; // get the command label

        if (containsUppercase(commandLabel)) {
            String labelLowerCase = commandLabel.toLowerCase(); // get the lowercase command label

            StringJoiner stringJoiner = new StringJoiner(" ");
            stringJoiner.add(labelLowerCase);

            if (parts.length > 1) {
                stringJoiner.add(parts[1]);
            }

            sendCommand(stringJoiner.toString());
            return false;
        }

        Matcher commandNaviHouseNumberMatcher = COMMAND_NAVI_HOUSE_NUMBER_PATTERN.matcher(command);
        if (commandNaviHouseNumberMatcher.find()) {
            String number = commandNaviHouseNumberMatcher.group("number");
            sendCommand("navi Haus:" + number);
            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        return storage.getCommandResponseRetrievers().stream()
                .filter(CommandResponseRetriever::isActive)
                .noneMatch(commandResponseRetriever -> commandResponseRetriever.addAsResultIfMatch(message));
    }

    private boolean containsUppercase(@NotNull String input) {
        for (char c : input.toCharArray()) {
            if (isUpperCase(c)) {
                return true;
            }
        }

        return false;
    }
}
