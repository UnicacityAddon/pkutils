package de.rettichlp.pkutils.common;

import de.rettichlp.pkutils.common.models.BlackMarket;
import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.BlacklistReason;
import de.rettichlp.pkutils.common.models.ContractEntry;
import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.models.HousebanEntry;
import de.rettichlp.pkutils.common.models.Job;
import de.rettichlp.pkutils.common.models.Reinforcement;
import de.rettichlp.pkutils.common.models.WantedEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.util.Arrays.stream;

public class Storage {

    @Getter
    private final Map<Faction, Set<FactionMember>> factionMembers = new HashMap<>();

    @Getter
    private final List<BlacklistEntry> blacklistEntries = new ArrayList<>();

    @Getter
    private final Map<Faction, List<BlacklistReason>> blacklistReasons = new HashMap<>();

    @Getter
    private final List<ContractEntry> contractEntries = new ArrayList<>();

    @Getter
    private final List<Countdown> countdowns = new ArrayList<>();

    @Getter
    private final List<HousebanEntry> housebanEntries = new ArrayList<>();

    @Getter
    private final List<Reinforcement> reinforcements = new ArrayList<>();

    @Getter
    private final Map<String, Integer> retrievedNumbers = new HashMap<>();

    @Getter
    private final List<BlackMarket> blackMarkets = new ArrayList<>();

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    @Getter
    @Setter
    private boolean afk = false;

    @Getter
    @Setter
    @Nullable
    private Job currentJob;

    @Getter
    @Setter
    private MinecartEntity minecartEntityToHighlight;

    @Getter
    @Setter
    private int moneyAtmAmount = 0;

    @Getter
    @Setter
    private int moneyBankAmount = 0;

    @Getter
    @Setter
    private ToggledChat toggledChat = NONE;

    {
        this.blackMarkets.addAll(stream(BlackMarket.Type.values())
                .map(type -> new BlackMarket(type, null, false))
                .toList());
    }

    public void print() {
        // factionMembers
        this.factionMembers.forEach((faction, factionMembers) -> LOGGER.info("factionMembers[{}:{}]: {}", faction, factionMembers.size(), factionMembers));
        // blacklistEntries
        LOGGER.info("blacklistEntries[{}]: {}", this.blacklistEntries.size(), this.blacklistEntries);
        // blacklistReasons
        this.blacklistReasons.forEach((faction, blacklistReasons) -> LOGGER.info("blacklistReasons[{}:{}]: {}", faction, blacklistReasons.size(), blacklistReasons));
        // contractEntries
        LOGGER.info("contractEntries[{}]: {}", this.contractEntries.size(), this.contractEntries);
        // countdowns
        LOGGER.info("countdowns[{}]: {}", this.countdowns.size(), this.countdowns);
        // housebanEntries
        LOGGER.info("housebanEntries[{}]: {}", this.housebanEntries.size(), this.housebanEntries);
        // reinforcements
        LOGGER.info("reinforcements[{}]: {}", this.reinforcements.size(), this.reinforcements);
        // retrievedNumbers
        LOGGER.info("retrievedNumbers[{}]: {}", this.retrievedNumbers.size(), this.retrievedNumbers);
        // visitedBlackMarkets
        LOGGER.info("blackMarkets[{}]: {}", this.blackMarkets.size(), this.blackMarkets);
        // wantedEntries
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
        // currentJob
        LOGGER.info("currentJob: {}", this.currentJob);
        // minecartEntityToHighlight
        LOGGER.info("minecartEntityToHighlight: {}", this.minecartEntityToHighlight);
        // moneyAtmAmount
        LOGGER.info("moneyAtmAmount: {}", this.moneyAtmAmount);
        // moneyBankAmount
        LOGGER.info("moneyBankAmount: {}", this.moneyBankAmount);
        // toggledChat
        LOGGER.info("toggledChat: {}", this.toggledChat);
    }

    public Set<FactionMember> getFactionMembers(Faction faction) {
        return this.factionMembers.getOrDefault(faction, new HashSet<>());
    }

    public Faction getFaction(String playerName) {
        return this.factionMembers.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(factionMember -> factionMember.playerName().equals(playerName)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(NULL);
    }

    public void trackReinforcement(Reinforcement reinforcement) {
        // remove all previous reinforcements of the same sender
        this.reinforcements.removeIf(r -> r.getSenderPlayerName().equals(reinforcement.getSenderPlayerName()));
        // add new reinforcement
        this.reinforcements.add(reinforcement);
    }

    @Getter
    @AllArgsConstructor
    public enum ToggledChat {

        NONE("", "Dauerhafter Chat deaktiviert"),
        D_CHAT("d", "Dauerhafter D-Chat aktiviert"),
        F_CHAT("f", "Dauerhafter F-Chat aktiviert"),
        W_CHAT("w", "Dauerhafter Flüster-Chat aktiviert");

        private final String command;
        private final String toggleMessage;
    }
}
