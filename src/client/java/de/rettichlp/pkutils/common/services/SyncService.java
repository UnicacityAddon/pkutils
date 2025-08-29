package de.rettichlp.pkutils.common.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.storage.schema.BlacklistReason;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.FBI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
import static java.nio.charset.StandardCharsets.UTF_8;
import static de.rettichlp.pkutils.common.models.Faction.FBI;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static de.rettichlp.pkutils.common.models.Faction.POLIZEI;
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
    private boolean abortGameSyncProcess = false;

    public void executeSync() {
        this.abortGameSyncProcess = false;
        this.gameSyncProcessActive = true;
        notificationService.sendNotification("PKUtils wird synchronisiert...", CYAN, Faction.values().length * 1000L + 1000);

        // without scheduled timings
        parseBlacklistEntries();

        // seconds 1-13: execute commands for all factions -> blocks command input for 13 * 1000 ms
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> {
                if (this.abortGameSyncProcess) {
                    return;
                }

                networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName());
                notificationService.sendNotification("Synchronisiere Fraktion " + faction.getDisplayName() + "...", WHITE, 1000);
            }, 1000 * faction.ordinal());
        }

        // second 13: faction-related init commands
        delayedAction(() -> {
            if (this.abortGameSyncProcess) {
                return;
            }

            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());

            if (faction.isBadFaction()) {
                networkHandler.sendChatCommand("blacklist");
            } else if (faction == FBI || faction == POLIZEI) {
                networkHandler.sendChatCommand("wanteds");
            }

            notificationService.sendNotification("Synchronisiere fraktionsabhängige Daten...", WHITE, 1000);
        }, Faction.values().length * 1000L);

        // end: init commands dons
        delayedAction(() -> {
            if (this.abortGameSyncProcess) {
                return;
            }

            // api login
            api.registerPlayer();

            this.gameSyncProcessActive = false;
            notificationService.sendSuccessNotification("PKUtils synchronisiert");
            this.lastSyncTimestamp = now();
        }, Faction.values().length * 1000L + 1000);
    }

    public void stopSync() {
        this.abortGameSyncProcess = true;
        this.gameSyncProcessActive = false;
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        networkHandler.sendChatCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private void parseBlacklistEntries() {
        new Thread(() -> {
            storage.getBlacklistReasons().clear();

            try (InputStreamReader reader = new InputStreamReader(URI.create("https://gist.githubusercontent.com/rettichlp/54e97f4dbb3988bf22554c01d62af666/raw/pkutils-blacklistreasons.json").toURL().openStream(), UTF_8)) {
                Type type = new TypeToken<Map<Faction, List<BlacklistReason>>>(){}.getType();
                Map<Faction, List<BlacklistReason>> factionBlacklistReasons = new Gson().fromJson(reader, type);
                factionBlacklistReasons.forEach((faction, blacklistReasons) -> storage.getBlacklistReasons().put(faction, blacklistReasons));
            } catch (Exception e) {
                LOGGER.error("Failed to fetch blacklist reasons", e);
            }
        }).start();
    }
}
