package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class BlacklistListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern BLACKLIST_HEADER_PATTERN = compile("^==== Blacklist .+ ====$");
    private static final Pattern BLACKLIST_ENTRY_PATTERN = compile("^ » (?<playerName>[a-zA-Z0-9_]+) \\| (?<reason>.+) \\| (?<dateTime>.+) \\| (?<kills>\\d+) Kills \\| (?<price>\\d+)\\$(| \\(AFK\\))$");
    private static final Pattern BLACKLIST_ENTRY_ADD = compile("^\\[Blacklist] (?<targetName>[a-zA-Z0-9_]+) wurde von (?<playerName>[a-zA-Z0-9_]+) auf die Blacklist gesetzt!$");
    private static final Pattern BLACKLIST_ENTRY_REMOVE = compile("^\\[Blacklist] (?<targetName>[a-zA-Z0-9_]+) wurde von (?<playerName>[a-zA-Z0-9_]+) von der Blacklist gelöscht!$");

    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher blacklistHeaderMatcher = BLACKLIST_HEADER_PATTERN.matcher(message);
        if (blacklistHeaderMatcher.find()) {
            this.activeCheck = currentTimeMillis();
            storage.getBlacklistEntries().clear();
            return !syncService.isGameSyncProcessActive();
        }

        Matcher blacklistEntryMatcher = BLACKLIST_ENTRY_PATTERN.matcher(message);
        if (blacklistEntryMatcher.find() && (currentTimeMillis() - this.activeCheck < 100)) {
            String playerName = blacklistEntryMatcher.group("playerName");
            String reason = blacklistEntryMatcher.group("reason");
            boolean outlaw = reason.toLowerCase().contains("(vf)") || reason.toLowerCase().contains("(vogelfrei)");
            int kills = parseInt(blacklistEntryMatcher.group("kills"));
            int price = parseInt(blacklistEntryMatcher.group("price"));

            BlacklistEntry blacklistEntry = new BlacklistEntry(playerName, reason, outlaw, kills, price);
            storage.getBlacklistEntries().add(blacklistEntry);
            return !syncService.isGameSyncProcessActive();
        }

        Matcher blacklistEntryAddMatcher = BLACKLIST_ENTRY_ADD.matcher(message);
        if (blacklistEntryAddMatcher.find()) {
            // show all entries to sync
            delayedAction(() -> sendCommand("blacklist"), 1000);
            return true;
        }

        Matcher blacklistEntryRemoveMatcher = BLACKLIST_ENTRY_REMOVE.matcher(message);
        if (blacklistEntryRemoveMatcher.find()) {
            String targetName = blacklistEntryRemoveMatcher.group("targetName");
            storage.getBlacklistEntries().removeIf(blacklistEntry -> blacklistEntry.getPlayerName().equals(targetName));
            return true;
        }

        return true;
    }
}
