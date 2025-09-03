package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.api.schema.request.UserRegisterRequest;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.FBI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
import static java.awt.Color.CYAN;
import static java.awt.Color.WHITE;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void executeSync() {
        this.gameSyncProcessActive = true;
        hudService.sendNotification("PKUtils wird synchronisiert...", CYAN, Faction.values().length * 1000L + 1000);

        // api login
        Request<UserRegisterRequest> request = Request.<UserRegisterRequest>builder()
                .body(new UserRegisterRequest())
                .build();
        request.send(response -> hudService.sendSuccessNotification("API Login erfolgreich"), errorResponse -> hudService.sendErrorNotification("API Login fehlgeschlagen"));

        // seconds 1-13: execute commands for all factions -> blocks command input for 13 * 1000 ms
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> {
                networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName());
                hudService.sendNotification("Synchronisiere Fraktion " + faction.getDisplayName() + "...", WHITE, 1000);
            }, 1000 * faction.ordinal());
        }

        // second 13: faction-related init commands
        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());

            if (faction.isBadFaction()) {
                networkHandler.sendChatCommand("blacklist");
            } else if (faction == FBI || faction == POLIZEI) {
                networkHandler.sendChatCommand("wanteds");
            }

            hudService.sendNotification("Synchronisiere fraktionsabhängige Daten...", WHITE, 1000);
        }, Faction.values().length * 1000L);

        // end: init commands dons
        delayedAction(() -> {
            this.gameSyncProcessActive = false;
            hudService.sendSuccessNotification("PKUtils synchronisiert");
            this.lastSyncTimestamp = now();
        }, Faction.values().length * 1000L + 1000);
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        networkHandler.sendChatCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }
}
