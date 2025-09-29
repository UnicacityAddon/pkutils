package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.ContractEntry;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getLogger;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class ContractListener extends PKUtilsBase implements IMessageReceiveListener {
    private static final Pattern CONTRACT_HEADER_PATTERN = compile("^\\[Contracts] Kopfgelder:"); //TODO
    private static final Pattern CONTRACT_ENTRY_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) (?<price>.+) (?<afk> \\| (AFK)|)");//TODO
    private static final Pattern CONTRACT_ENTRY_ADD = compile("^\\[Contract] Es wurde ein Kopfgeld auf (?:\\[PK])?(?<targetName>[a-zA-Z0-9_]+) (?<price>.[0-9]+) ausgesetzt.$");//TODO
    private static final Pattern CONTRACT_ENTRY_REMOVE = compile("^\\[Contract] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[PK])?(?<targetName>[a-zA-Z0-9_]+) getötet.$");//TODO
    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(Text text, String message) {

        Matcher contractHeaderMatcher = CONTRACT_HEADER_PATTERN.matcher(message);
        if (contractHeaderMatcher.find()) {
            this.activeCheck = currentTimeMillis();
            storage.getContractEntries().clear();

            return !syncService.isGameSyncProcessActive();
        }

        Matcher contractEntryMatcher = CONTRACT_ENTRY_PATTERN.matcher(message);
        if (contractEntryMatcher.find() && (currentTimeMillis() - this.activeCheck < 100)) {
            String playerName = contractEntryMatcher.group("playerName");
            int price = parseInt(contractEntryMatcher.group("price"));
            ContractEntry contractEntry = new ContractEntry(playerName, price);
            storage.getContractEntries().add(contractEntry);
            return !syncService.isGameSyncProcessActive();
        }

        Matcher contractEntryAddMatcher = CONTRACT_ENTRY_ADD.matcher(message);
        if (contractEntryAddMatcher.find()) {
            // show all entries to sync
            delayedAction(() -> sendCommand("contract"), 1000);
            return true;
        }

        Matcher contractEntryRemoveMatcher = CONTRACT_ENTRY_REMOVE.matcher(message);
        if (contractEntryRemoveMatcher.find()) {
            String targetName = contractEntryRemoveMatcher.group("targetName");
            storage.getContractEntries().removeIf(contractEntry -> contractEntry.getPlayerName().equals(targetName));
            return true;
        }

        return true;
    }
}
