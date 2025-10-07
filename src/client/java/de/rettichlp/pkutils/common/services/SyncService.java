package de.rettichlp.pkutils.common.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import de.rettichlp.pkutils.common.models.BlacklistReason;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static de.rettichlp.pkutils.common.models.Faction.TRIADEN;
import static java.awt.Color.CYAN;
import static java.awt.Color.WHITE;
import static java.nio.charset.StandardCharsets.UTF_8;
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
        notificationService.sendNotification("PKUtils wird synchronisiert...", CYAN, Faction.values().length * 1000L + 1000);

        // parse blacklist reasons from GitHub Gist
        syncBlacklistEntries();

        // parse faction data
        for (Faction faction : Faction.values()) {
            if (faction == NULL || faction == TRIADEN) {
                continue;
            }

            syncFactionMembers(faction);
        }

        // parse from faction-related init commands
        notificationService.sendNotification("Synchronisiere fraktionsabhängige Daten...", WHITE, 1000);
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

        // sync with api
        api.registerUser(getVersion());

        this.gameSyncProcessActive = false;
        this.lastSyncTimestamp = now();
        notificationService.sendSuccessNotification("PKUtils synchronisiert");
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private void syncBlacklistEntries() {
        new Thread(() -> {
            storage.getBlacklistReasons().clear();

            try (InputStreamReader reader = new InputStreamReader(URI.create("https://gist.githubusercontent.com/rettichlp/54e97f4dbb3988bf22554c01d62af666/raw/pkutils-blacklistreasons.json").toURL().openStream(), UTF_8)) {
                Type type = new TypeToken<Map<String, List<BlacklistReason>>>() {}.getType();
                Map<String, List<BlacklistReason>> factionBlacklistReasons = new Gson().fromJson(reader, type);
                factionBlacklistReasons.forEach((factionString, blacklistReasons) -> storage.getBlacklistReasons().put(Faction.valueOf(factionString), blacklistReasons));
            } catch (Exception e) {
                LOGGER.error("Failed to fetch blacklist reasons", e);
            }
        }).start();
    }

    private void syncFactionMembers(Faction faction) {
        api.getFactionData(faction).thenAccept(objectMap -> {
            Gson gson = api.getGson();
            String jsonString = gson.toJson(objectMap);

            Type type = new TypeToken<List<FactionMember>>() {}.getType();
            Set<FactionMember> members = gson.fromJson(jsonString, type);

            storage.getFactionMembers().put(faction, members);
        });
    }
}
