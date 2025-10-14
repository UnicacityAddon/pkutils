package de.rettichlp.pkutils.common.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.rettichlp.pkutils.common.models.CommandResponseRetriever;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static de.rettichlp.pkutils.common.models.Faction.TRIADEN;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void syncFactionMembersWithApi() {
        api.getFactionEntries().thenAccept(factionEntries -> {
            factionEntries.forEach(factionEntry -> storage.getFactionMembers().put(factionEntry.faction(), new HashSet<>(factionEntry.members())));
            LOGGER.info("Faction members synced with API");
        });
    }

    public void syncFactionMembersWithCommandResponse() {
        List<CommandResponseRetriever> commandResponseRetrievers = stream(Faction.values())
                .filter(faction -> faction != NULL && faction != TRIADEN)
                .map(this::syncFactionMembersWithCommandResponse)
                .toList();

        for (int i = 0; i < commandResponseRetrievers.size(); i++) {
            CommandResponseRetriever commandResponseRetriever = commandResponseRetrievers.get(i);
            delayedAction(commandResponseRetriever::execute, i * 1000L);
        }

        delayedAction(api::postFactionEntries, commandResponseRetrievers.size() * 1000L + 1200);
    }

    public void syncBlacklistReasonsFromApi() {
        api.getBlacklistReasonData().thenAccept(factionListMap -> {
            storage.getBlacklistReasons().clear();
            storage.getBlacklistReasons().putAll(factionListMap);
        }).thenAccept(unused -> LOGGER.info("Blacklist reason data synced"));
    }

    public void syncFactionSpecificData() {
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

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }

    private @NotNull CompletableFuture<Void> syncFactionMembersWithApi(Faction faction) {
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
            LOGGER.info("Retrieved {} members for faction {} from api", factionMembers.size(), faction.name());
        });
    }

    @Contract("_ -> new")
    private @NotNull CommandResponseRetriever syncFactionMembersWithCommandResponse(@NotNull Faction faction) {
        String commandToExecute = "memberinfoall " + faction.getMemberInfoCommandName();
        return new CommandResponseRetriever(commandToExecute, FACTION_MEMBER_ALL_ENTRY, matchers -> {
            Set<FactionMember> factionMembers = new HashSet<>();

            matchers.forEach(matcher -> {
                int rank = parseInt(matcher.group("rank"));
                String[] playerNames = matcher.group("playerNames").split(", ");

                for (String playerName : playerNames) {
                    FactionMember factionMember = new FactionMember(playerName, rank);
                    factionMembers.add(factionMember);
                }
            });

            storage.getFactionMembers().put(faction, factionMembers);
            LOGGER.info("Retrieved {} members for faction {} from command", factionMembers.size(), faction.name());
        }, true);
    }
}
