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
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static de.rettichlp.pkutils.common.models.Faction.TRIADEN;
import static java.awt.Color.WHITE;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.allOf;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void executeSync() {
        this.gameSyncProcessActive = true;
        notificationService.sendInfoNotification("PKUtils wird synchronisiert...");

        // parse faction data
        CompletableFuture<?>[] syncFactionMembersFutures = stream(Faction.values())
                .filter(faction -> faction != NULL && faction != TRIADEN)
                .map(this::syncFactionMemberData)
                .toArray(CompletableFuture[]::new);

        // run each task sequentially
        CompletableFuture<Void> overallFuture = syncBlacklistReasonData() // parse blacklist reasons from GitHub Gist
                .thenCompose(unused -> allOf(syncFactionMembersFutures)) // run all sync tasks in parallel
                .thenCompose(unused -> api.registerUser(getVersion())); // login to PKUtils API

        // parse from faction-related init commands after all faction members are synced
        overallFuture.thenAccept(unused -> {
            notificationService.sendInfoNotification("Synchronisiere fraktionsabhängige Daten...");

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

            this.lastSyncTimestamp = now();

            delayedAction(() -> {
                this.gameSyncProcessActive = false;
                notificationService.sendSuccessNotification("PKUtils synchronisiert");
            }, 200);
        }).exceptionally(throwable -> {
            LOGGER.error("Error while syncing process", throwable);
            return null;
        });
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private @NotNull CompletableFuture<Void> syncBlacklistReasonData() {
        return api.getBlacklistReasonData().thenAccept(factionListMap -> {
            storage.getBlacklistReasons().clear();
            storage.getBlacklistReasons().putAll(factionListMap);
        });
    }

    private @NotNull CompletableFuture<Void> syncFactionMemberData(Faction faction) {
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
