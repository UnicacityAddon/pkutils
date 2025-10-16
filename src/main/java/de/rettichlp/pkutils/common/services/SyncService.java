package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.models.CommandResponseRetriever;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionEntry;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.awt.Color.MAGENTA;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.regex.Pattern.compile;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void syncFactionMembersWithApi() {
        api.getFactions(factionEntries -> {
            storage.getPlayerFactionCache().clear();
            storage.getFactionEntries().clear();
            storage.getFactionEntries().addAll(factionEntries);
            LOGGER.info("Faction members synced with API");
        });
    }

    public void syncFactionMembersWithCommandResponse() {
        storage.getPlayerFactionCache().clear();

        List<CommandResponseRetriever> commandResponseRetrievers = stream(Faction.values())
                .filter(faction -> faction != NULL)
                .map(this::syncFactionMembersWithCommandResponse)
                .toList();

        for (int i = 0; i < commandResponseRetrievers.size(); i++) {
            CommandResponseRetriever commandResponseRetriever = commandResponseRetrievers.get(i);
            delayedAction(commandResponseRetriever::execute, i * 1000L);
        }

        delayedAction(api::postFactions, commandResponseRetrievers.size() * 1000L + 1200);
    }

    public void syncBlacklistReasonsFromApi() {
        api.getBlacklistReasonData(factionListMap -> {
            storage.getBlacklistReasons().clear();
            storage.getBlacklistReasons().putAll(factionListMap);
        });
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

    public void checkForUpdates() {
        api.getModrinthVersions(maps -> {
            if (maps.isEmpty()) {
                return;
            }

            Map<String, Object> latestRelease = maps.getFirst();
            String latestVersion = (String) latestRelease.get("version_number");

            String currentVersion = getVersion();
            if (nonNull(latestVersion) && !currentVersion.equals(latestVersion)) {
                notificationService.sendNotification(() -> Text.empty()
                        .append(Text.of("Neue PKUtils Version verfügbar:").copy().formatted(GRAY)).append(" ")
                        .append(Text.of(currentVersion).copy().formatted(RED)).append(" ")
                        .append(Text.of("→").copy().formatted(GRAY)).append(" ")
                        .append(Text.of(latestVersion).copy().formatted(GREEN)), MAGENTA, MINUTES.toMillis(5));
            }
        });
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        sendCommand("nummer " + playerName);

        delayedAction(() -> ofNullable(storage.getRetrievedNumbers().get(playerName))
                .ifPresentOrElse(runWithNumber, () -> sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false)), 1000);
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

            FactionEntry factionEntry = new FactionEntry(faction, factionMembers);
            storage.getFactionEntries().removeIf(fe -> fe.faction() == faction);
            storage.getFactionEntries().add(factionEntry);
            LOGGER.info("Retrieved {} members for faction {} from command", factionMembers.size(), faction.name());
        }, true);
    }
}
