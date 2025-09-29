package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.ContractEntry;
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
public class ContractListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern CONTRACT_HEADER_PATTERN = compile("^\\[Contracts] Kopfgelder:");
    private static final Pattern CONTRACT_ENTRY_PATTERN = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) \\[(?<price>\\d+)\\$](?:\\(AFK\\))?$");
    private static final Pattern CONTRACT_ADD_PATTERN = compile("^\\[Contract] Es wurde ein Kopfgeld auf (?:\\[PK])?(?<targetName>[a-zA-Z0-9_]+) \\((?<price>\\d+)\\$\\) ausgesetzt\\.$");
    private static final Pattern CONTRACT_REMOVE_PATTERN = compile("^\\[Contract] Das Kopfgeld auf (?:\\[PK])?(?<targetName>[a-zA-Z0-9_]+) wurde entfernt\\.$");
    private static final Pattern CONTRACT_KILL_PATTERN = compile("^\\[Contract] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[PK])?(?<targetName>[a-zA-Z0-9_]+) getötet\\. \\((?<price>\\d+)\\$\\)$");

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

        Matcher contractAddMatcher = CONTRACT_ADD_PATTERN.matcher(message);
        if (contractAddMatcher.find()) {
            // show all entries to sync
            delayedAction(() -> sendCommand("contract"), 1000);
            return true;
        }

        Matcher contractRemoveMatcher = CONTRACT_REMOVE_PATTERN.matcher(message);
        if (contractRemoveMatcher.find()) {
            String targetName = contractRemoveMatcher.group("targetName");
            storage.getContractEntries().removeIf(contractEntry -> contractEntry.getPlayerName().equals(targetName));
            return true;
        }

        Matcher contractKillMatcher = CONTRACT_KILL_PATTERN.matcher(message);
        if (contractKillMatcher.find()) {
            String targetName = contractKillMatcher.group("targetName");
            storage.getContractEntries().removeIf(contractEntry -> contractEntry.getPlayerName().equals(targetName));
            return true;
        }

        return true;
    }
}
