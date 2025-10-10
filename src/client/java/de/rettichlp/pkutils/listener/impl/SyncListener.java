package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static java.lang.Integer.parseInt;
import static java.time.Duration.ofMinutes;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class SyncListener extends PKUtilsBase implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern SERVER_PASSWORD_MISSING_PATTERN = compile("^» Schütze deinen Account mit /passwort new \\[Passwort]$");
    private static final Pattern SERVER_PASSWORD_ACCEPTED_PATTERN = compile("^Du hast deinen Account freigeschaltet\\.$");

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (syncService.isGameSyncProcessActive() && !command.contains("wanteds") && !command.contains("contractlist") && !command.contains("hausverbot list") && !command.contains("blacklist")) {
            notificationService.sendWarningNotification("Synchronisierung aktiv - Befehle blockiert");
            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        // SERVER INIT

        // if a password is not set, start the game sync process
        Matcher serverPasswordMissingMatcher = SERVER_PASSWORD_MISSING_PATTERN.matcher(message);
        if (serverPasswordMissingMatcher.find()) {
            syncService.syncIngameData();
            return true;
        }

        // if a password is accepted, start the game sync process
        Matcher serverPasswordAcceptedMatcher = SERVER_PASSWORD_ACCEPTED_PATTERN.matcher(message);
        if (serverPasswordAcceptedMatcher.find()) {
            syncService.syncIngameData();
            return true;
        }

        return true;
    }
}
