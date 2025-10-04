package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.HousebanEntry;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class HousebanListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern HOUSEBAN_HEADER_PATTERN = compile("^Hausverbote \\(Rettungsdienst\\):$");
    private static final Pattern HOUSEBAN_ENTRY_PATTERN = compile("^(?<playerName>[a-zA-Z0-9_]+) \\| (?<issuerPlayerName>[a-zA-Z0-9_]+) \\| (?<reasons>.+) \\| (?<expireDateDay>\\d+)\\.(?<expireDateMonth>\\d+)\\.(?<expireDateYear>\\d+) (?<expireTimeHour>\\d+):(?<expireTimeMinute>\\d+) \\[Entfernen]$");
    private static final Pattern HOUSEBAN_ADD_PATTERN = compile("^(?<issuerPlayerName>[a-zA-Z0-9_]+) hat (?<playerName>[a-zA-Z0-9_]+) ein Hausverbot erteilt\\. \\((?<reason>.+) \\| Ende: (?<expireDateDay>\\d+)\\.(?<expireDateMonth>\\d+)\\.(?<expireDateYear>\\d+) (?<expireTimeHour>\\d+):(?<expireTimeMinute>\\d+)\\)$");

    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher housebanHeaderMatcher = HOUSEBAN_HEADER_PATTERN.matcher(message);
        if (housebanHeaderMatcher.find()) {
            this.activeCheck = currentTimeMillis();
            storage.getHousebanEntries().clear();
            return !syncService.isGameSyncProcessActive();
        }

        Matcher housebanEntryMatcher = HOUSEBAN_ENTRY_PATTERN.matcher(message);
        if (housebanEntryMatcher.find() && currentTimeMillis() - this.activeCheck < 100) {
            String playerName = housebanEntryMatcher.group("playerName");
            String issuerPlayerName = housebanEntryMatcher.group("issuerPlayerName");
            String reasonsRaw = housebanEntryMatcher.group("reasons");
            int expireDateDay = parseInt(housebanEntryMatcher.group("expireDateDay"));
            int expireDateMonth = parseInt(housebanEntryMatcher.group("expireDateMonth"));
            int expireDateYear = parseInt(housebanEntryMatcher.group("expireDateYear"));
            int expireTimeHour = parseInt(housebanEntryMatcher.group("expireTimeHour"));
            int expireTimeMinute = parseInt(housebanEntryMatcher.group("expireTimeMinute"));

            String[] reasons = reasonsRaw.split(" \\+ ");

            LocalDateTime unbanDateTime = LocalDateTime.of(expireDateYear, expireDateMonth, expireDateDay, expireTimeHour, expireTimeMinute);
            HousebanEntry housebanEntry = new HousebanEntry(playerName, issuerPlayerName, asList(reasons), unbanDateTime);
            storage.getHousebanEntries().add(housebanEntry);
            return !syncService.isGameSyncProcessActive();
        }

        Matcher housebanAddMatcher = HOUSEBAN_ADD_PATTERN.matcher(message);
        if (housebanAddMatcher.find()) {
            String playerName = housebanAddMatcher.group("playerName");
            String issuerPlayerName = housebanAddMatcher.group("issuerPlayerName");
            String reason = housebanAddMatcher.group("reason");
            int expireDateDay = parseInt(housebanAddMatcher.group("expireDateDay"));
            int expireDateMonth = parseInt(housebanAddMatcher.group("expireDateMonth"));
            int expireDateYear = parseInt(housebanAddMatcher.group("expireDateYear"));
            int expireTimeHour = parseInt(housebanAddMatcher.group("expireTimeHour"));
            int expireTimeMinute = parseInt(housebanAddMatcher.group("expireTimeMinute"));

            storage.getHousebanEntries().removeIf(housebanEntry -> housebanEntry.getPlayerName().equals(playerName));
            LocalDateTime unbanDateTime = LocalDateTime.of(expireDateYear, expireDateMonth, expireDateDay, expireTimeHour, expireTimeMinute);
            HousebanEntry housebanEntry = new HousebanEntry(playerName, issuerPlayerName, singletonList(reason), unbanDateTime);
            storage.getHousebanEntries().add(housebanEntry);
            return true;
        }

        return true;
    }
}
