package de.rettichlp.pkutils.common.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.networkHandler;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static java.lang.Boolean.getBoolean;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class CommandService {

    public void sendCommand(String command) {
        LOGGER.info("PKUtils executing command: {}", command);
        networkHandler.sendChatCommand(command);
    }

    public boolean sendCommandWithAfkCheck(String command) {
        boolean isAfk = storage.isAfk();
        LOGGER.info("PKUtils executing command with AFK check (is AFK: {}): {}", isAfk, command);

        if (!isAfk) {
            networkHandler.sendChatCommand(command);
        }

        return !isAfk;
    }

    public void sendCommands(List<String> commandStrings) {
        // to modifiable list
        List<String> commands = new ArrayList<>(commandStrings);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (commands.isEmpty()) {
                    this.cancel();
                    return;
                }

                sendCommand(commands.removeFirst());
            }
        }, 0, 1000);
    }

    public boolean isSuperUser() {
        return nonNull(player) && (player.getUuidAsString().equals("25855f4d-3874-4a7f-a6ad-e9e4f3042e19") || getBoolean("fabric.development"));
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        utilService.delayedAction(() -> ofNullable(storage.getRetrievedNumbers().get(playerName))
                .ifPresentOrElse(runWithNumber, () -> messageService.sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false)), 1000);
    }
}
