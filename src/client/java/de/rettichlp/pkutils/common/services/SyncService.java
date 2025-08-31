package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.api.schema.request.FactionSyncRequest;
import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.FBI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
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
        sendModMessage("PKUtils wird synchronisiert...", false);

        // seconds 1-13: execute commands for all factions -> blocks command input for 13 * 1000 ms
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName()), 1000 * faction.ordinal());
        }

        // second 13: faction-related init commands
        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());

            if (faction.isBadFaction()) {
                networkHandler.sendChatCommand("blacklist");
            } else if (faction == FBI || faction == POLIZEI) {
                networkHandler.sendChatCommand("wanteds");
            }
        }, Faction.values().length * 1000L);

        // end: init commands dons
        delayedAction(() -> {
            this.gameSyncProcessActive = false;
            sendModMessage("PKUtils synchronisiert.", false);
            this.lastSyncTimestamp = now();
            pushFactionDataToServer();
        }, Faction.values().length * 1000L + 200);
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        networkHandler.sendChatCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private void pushFactionDataToServer() {
        List<Map<String, Object>> membersData = new ArrayList<>();
        storage.getFactionMembers().forEach((faction, members) -> {
            if (faction != NULL) {
                members.forEach(member -> {
                    Map<String, Object> memberMap = new HashMap<>();
                    memberMap.put("playerName", member.getPlayerName());
                    memberMap.put("faction", faction.getDisplayName());
                    memberMap.put("rank", member.getRank());
                    membersData.add(memberMap);
                });
            }
        });

        Request<FactionSyncRequest> request = Request.<FactionSyncRequest>builder()
                .body(new FactionSyncRequest(player.getName().getString(), membersData))
                .build();

        request.send(
                response -> {
                    if (response.statusCode() == 200) {
                        sendModMessage("Fraktionsdaten zum Server synchronisiert.", true);
                    } else {
                        sendModMessage("Fehler bei der Server-Synchronisation.", true);
                    }
                },
                throwable -> sendModMessage("Fehler bei der Server-Synchronisation.", true)
        );
    }
}