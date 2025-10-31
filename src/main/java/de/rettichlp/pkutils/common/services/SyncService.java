package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.models.CommandResponseRetriever;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionEntry;
import de.rettichlp.pkutils.common.models.FactionMember;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.awt.Color.MAGENTA;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;

public class SyncService {

    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");
    private static final int REQUIRED_DATA_USAGE_CONFIRMATION_UID = 1;

    @Getter
    private LocalDateTime lastSyncTimestamp = MIN;
    @Getter
    private boolean gameSyncProcessActive = false;

    public boolean dataUsageConfirmed() {
        int currentDataUsageConfirmationUID = configuration.getDataUsageConfirmationUID();
        return currentDataUsageConfirmationUID >= REQUIRED_DATA_USAGE_CONFIRMATION_UID;
    }

    public void updateDataUsageConfirmedUID() {
        configuration.setDataUsageConfirmationUID(REQUIRED_DATA_USAGE_CONFIRMATION_UID);
    }

    public void sync(boolean showPopupIfNotGiven) {
        boolean dataUsageConfirmed = dataUsageConfirmed();

        if (dataUsageConfirmed) {
            LOGGER.info("Data usage confirmed, proceeding with sync...");

            // sync faction members
            syncFactionMembersWithApi();
            // sync blacklist reasons
            syncBlacklistReasonsFromApi();
            // check for updates
            checkForUpdates();

            // login to PKUtils API
            api.postUserRegister();
        } else if (showPopupIfNotGiven) {
            LOGGER.info("Data usage not yet confirmed, showing confirmation popup");
            showDataUsageConfirmationPopup();
        } else {
            LOGGER.info("Data usage not confirmed, skipping sync");
        }

        // always sync faction specific data (no external data)
        syncFactionSpecificData();
    }

    public void syncFactionMembersWithCommandResponse() {
        List<CommandResponseRetriever> commandResponseRetrievers = stream(Faction.values())
                .filter(faction -> faction != NULL)
                .map(this::syncFactionMembersWithCommandResponse)
                .toList();

        for (int i = 0; i < commandResponseRetrievers.size(); i++) {
            CommandResponseRetriever commandResponseRetriever = commandResponseRetrievers.get(i);
            utilService.delayedAction(commandResponseRetriever::execute, i * 1000L);
        }

        storage.getPlayerFactionCache().clear();
        utilService.delayedAction(api::postFactions, commandResponseRetrievers.size() * 1000L + 1200);
    }

    public void syncFactionSpecificData() {
        this.gameSyncProcessActive = true;
        this.lastSyncTimestamp = now();

        // parse from faction-related init commands after all faction members are synced
        utilService.delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());
            switch (faction) {
                case FBI, POLIZEI -> commandService.sendCommand("wanteds");
                case HITMAN -> commandService.sendCommand("contractlist");
                case RETTUNGSDIENST -> commandService.sendCommand("hausverbot list");
                default -> {
                    if (faction.isBadFaction()) {
                        commandService.sendCommand("blacklist");
                    }
                }
            }
        }, 1000);

        utilService.delayedAction(() -> this.gameSyncProcessActive = false, 2000);
    }

    private void syncFactionMembersWithApi() {
        api.getFactions(factionEntries -> {
            storage.getFactionEntries().clear();
            storage.getFactionEntries().addAll(factionEntries);
            storage.getPlayerFactionCache().clear();
            LOGGER.info("Faction members synced with API");
        });
    }

    private void syncBlacklistReasonsFromApi() {
        api.getBlacklistReasonData(factionListMap -> {
            storage.getBlacklistReasons().clear();
            storage.getBlacklistReasons().putAll(factionListMap);
        });
    }

    private void checkForUpdates() {
        api.getModrinthVersions(maps -> {
            if (maps.isEmpty()) {
                return;
            }

            Map<String, Object> latestRelease = maps.getFirst();
            String latestVersion = (String) latestRelease.get("version_number");

            String currentVersion = utilService.getVersion();
            if (nonNull(latestVersion) && !currentVersion.equals(latestVersion)) {
                notificationService.sendNotification(() -> empty()
                        .append(of("Neue PKUtils Version verfügbar").copy().formatted(GRAY))
                        .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                        .append(of(currentVersion).copy().formatted(RED)).append(" ")
                        .append(of("→").copy().formatted(GRAY)).append(" ")
                        .append(of(latestVersion).copy().formatted(GREEN)), MAGENTA, MINUTES.toMillis(5));
            }
        });
    }

    private void showDataUsageConfirmationPopup() {
        MinecraftClient client = MinecraftClient.getInstance();

        PopupScreen dataUsageConfirmationScreen = new PopupScreen.Builder(client.currentScreen, empty().append(of("PKUtils")).append(" ").append(translatable("mco.terms.sentence.2")))
                .message(translatable("pkutils.screen.data_usage_confirmation.message"))
                .button(translatable("mco.terms.buttons.agree"), popupScreen -> {
                    updateDataUsageConfirmedUID();
                    sync(false);
                    popupScreen.close();
                })
                .button(translatable("mco.terms.buttons.disagree"), Screen::close)
                .build();

        client.execute(() -> client.setScreen(dataUsageConfirmationScreen));
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
