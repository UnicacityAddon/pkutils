package de.rettichlp.pkutils.common.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void syncBlacklistReasonData() {
        api.getBlacklistReasonData().thenAccept(factionListMap -> {
            storage.getBlacklistReasons().clear();
            storage.getBlacklistReasons().putAll(factionListMap);
        }).thenAccept(unused -> LOGGER.info("Blacklist reason data synced"));
    }

    public void syncFactionData() {
        api.getFactionEntries().thenAccept(factionEntries -> {
            factionEntries.forEach(factionEntry -> storage.getFactionMembers().put(factionEntry.faction(), new HashSet<>(factionEntry.members())));
            LOGGER.info("Faction member data synced");
        });
    }

    public void syncPKUtilsData() {
        api.registerUser(getVersion());
    }

    public void syncIngameData() {
        this.gameSyncProcessActive = true;
        this.lastSyncTimestamp = now();

        // parse from faction-related init commands after all faction members are synced
        notificationService.sendInfoNotification("Synchronisiere fraktionsabhängige Daten...");

        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());
            switch (faction) {
                case FBI, POLIZEI -> sendCommand("wanteds");
                case HITMAN -> sendCommand("contractlist");
                case RETTUNGSDIENST -> sendCommand("hausverbot list");
                default -> {
                    if (faction.isBadFaction()) {
                        sendCommand("blacklist");
                    }
                }
            }
        }, 1000);

        delayedAction(() -> {
            this.gameSyncProcessActive = false;
            notificationService.sendSuccessNotification("PKUtils synchronisiert");
        }, 2000);
    }

    public void executeSync() {
        syncBlacklistReasonData();
        syncFactionData();
        syncPKUtilsData();

        if (nonNull(player)) {
            syncIngameData();
        }
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private @NotNull CompletableFuture<Void> syncFactionData(Faction faction) {
        return api.getFactionMemberData(faction).thenAccept(objectMap -> {
            Gson gson = api.getGson();

            JsonArray members = gson.toJsonTree(objectMap.get("members")).getAsJsonArray();
            List<FactionMember> factionMembers = members.asList().stream()
                    .map(jsonElement -> {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        String username = jsonObject.get("username").getAsString();
                        int rank = jsonObject.get("rank").getAsInt();
                        return new FactionMember(username, rank);
                    })
                    .toList();

            storage.getFactionMembers().put(faction, new HashSet<>(factionMembers));
        });
    }
}
