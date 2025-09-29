package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static de.rettichlp.pkutils.common.Storage.Countdown.BANDAGE;
import static de.rettichlp.pkutils.common.Storage.Countdown.PILL;
import static de.rettichlp.pkutils.common.models.Faction.fromDisplayName;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class SyncListener extends PKUtilsBase implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern SERVER_PASSWORD_MISSING_PATTERN = compile("^» Schütze deinen Account mit /passwort new \\[Passwort]$");
    private static final Pattern SERVER_PASSWORD_ACCEPTED_PATTERN = compile("^Du hast deinen Account freigeschaltet\\.$");
    private static final Pattern SERVER_COMMAND_COOLDOWN_PATTERN = compile("^Bitte warte ein wenig, bis du erneut einen Befehl ausführst\\.$");
    private static final Pattern FACTION_MEMBER_ALL_HEADER = compile("^={4} Mitglieder von (?<factionName>.+) ={4}$");
    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");
    private static final Pattern MEDIC_BANDAGE_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat dich bandagiert\\.$");
    private static final Pattern MEDIC_PILL_PATTERN = compile("^\\[Medic] Doktor (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat dir Schmerzpillen verabreicht\\.$");
    private static final Pattern NUMBER_PATTERN = compile("^(?<playerName>[a-zA-Z0-9_]+) gehört die Nummer (?<number>\\d+)\\.$");

    private Faction factionMemberRetrievalFaction;
    private long factionMemberRetrievalTimestamp;

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (syncService.isGameSyncProcessActive() && !command.contains("memberinfoall") && !command.contains("wanteds") && !command.contains("blacklist")) {
            notificationService.sendWarningNotification("Synchronisierung aktiv - Befehle blockiert");
            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        // SERVER INIT

        // if a password is not set, start the game sync process
        Matcher serverPasswordMissingMatcher = SERVER_PASSWORD_MISSING_PATTERN.matcher(message);
        if (serverPasswordMissingMatcher.find()) {
            syncService.executeSync();
            return true;
        }

        // if a password is accepted, start the game sync process
        Matcher serverPasswordAcceptedMatcher = SERVER_PASSWORD_ACCEPTED_PATTERN.matcher(message);
        if (serverPasswordAcceptedMatcher.find()) {
            syncService.executeSync();
            return true;
        }

        // if a command cooldown message appears and a game sync process is active, stop the game sync process
        Matcher serverCommandCooldownMatcher = SERVER_COMMAND_COOLDOWN_PATTERN.matcher(message);
        if (serverCommandCooldownMatcher.find() && syncService.isGameSyncProcessActive()) {
            syncService.stopSync();
            notificationService.sendWarningNotification("Server Lag erkannt - Synchronisierung abgebrochen");
            return true;
        }

        // FACTION ALL INIT

        Matcher factionMemberAllHeaderMatcher = FACTION_MEMBER_ALL_HEADER.matcher(message);
        if (factionMemberAllHeaderMatcher.find()) {
            String factionName = factionMemberAllHeaderMatcher.group("factionName");
            this.factionMemberRetrievalTimestamp = currentTimeMillis();
            this.factionMemberRetrievalFaction = fromDisplayName(factionName)
                    .orElseThrow(() -> new IllegalStateException("Could not find faction with name: " + factionName));

            storage.getFactionMembers().put(this.factionMemberRetrievalFaction, new HashSet<>());
            return !syncService.isGameSyncProcessActive();
        }

        Matcher factionMemberAllEntryMatcher = FACTION_MEMBER_ALL_ENTRY.matcher(message);
        if (factionMemberAllEntryMatcher.find() && (currentTimeMillis() - this.factionMemberRetrievalTimestamp < 500) && nonNull(this.factionMemberRetrievalFaction)) {
            int rank = parseInt(factionMemberAllEntryMatcher.group("rank"));
            String[] playerNames = factionMemberAllEntryMatcher.group("playerNames")
                    .split(", ");

            for (String playerName : playerNames) {
                FactionMember factionMember = new FactionMember(playerName, rank);
                storage.addFactionMember(this.factionMemberRetrievalFaction, factionMember);
            }

            return !syncService.isGameSyncProcessActive();
        }

        // OTHER

        Matcher medicBandageMatcher = MEDIC_BANDAGE_PATTERN.matcher(message);
        if (medicBandageMatcher.find()) {
            storage.getCountdowns().put(BANDAGE, now());
            return true;
        }

        Matcher medicPillMatcher = MEDIC_PILL_PATTERN.matcher(message);
        if (medicPillMatcher.find()) {
            storage.getCountdowns().put(PILL, now());
            return true;
        }

        Matcher numberMatcher = NUMBER_PATTERN.matcher(message);
        if (numberMatcher.find()) {
            String playerName = numberMatcher.group("playerName");
            int number = parseInt(numberMatcher.group("number"));
            storage.getRetrievedNumbers().put(playerName, number);
        }

        return true;
    }
}
