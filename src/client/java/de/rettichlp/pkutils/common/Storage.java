package de.rettichlp.pkutils.common;

import de.rettichlp.pkutils.common.storage.schema.BlacklistEntry;
import de.rettichlp.pkutils.common.storage.schema.BlacklistReason;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.models.Reinforcement;
import de.rettichlp.pkutils.common.models.WantedEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.vehicle.MinecartEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

public class Storage {

    @Getter
    private final Map<Faction, Set<FactionMember>> factionMembers = new HashMap<>();

    @Getter
    private final List<BlacklistEntry> blacklistEntries = new ArrayList<>();

    @Getter
    private final Map<Faction, List<BlacklistReason>> blacklistReasons = new HashMap<>();

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    @Getter
    private final List<Reinforcement> reinforcements = new ArrayList<>();

    @Getter
    private final Map<String, Integer> retrievedNumbers = new HashMap<>();

    @Getter
    private final Map<Countdown, LocalDateTime> countdowns = new HashMap<>();

    @Getter
    @Setter
    private ToggledChat toggledChat = NONE;

    @Getter
    @Setter
    private MinecartEntity minecartEntityToHighlight;

    public void print() {
        // factionMembers
        this.factionMembers.forEach((faction, factionMembers) -> LOGGER.info("factionMembers[{}:{}]: {}", faction, factionMembers.size(), factionMembers));
        // blacklistEntries
        LOGGER.info("blacklistEntries[{}]: {}", this.blacklistEntries.size(), this.blacklistEntries);
        // wantedEntries
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
        // reinforcements
        LOGGER.info("reinforcements[{}]: {}", this.reinforcements.size(), this.reinforcements);
        // retrievedNumbers
        LOGGER.info("retrievedNumbers[{}]: {}", this.retrievedNumbers.size(), this.retrievedNumbers);
        // toggledChat
        LOGGER.info("toggledChat: {}", this.toggledChat);
    }

    public void addBlacklistEntry(BlacklistEntry entry) {
        this.blacklistEntries.add(entry);
    }

    public void resetBlacklistEntries() {
        this.blacklistEntries.clear();
    }

    public void addFactionMember(Faction faction, FactionMember factionMember) {
        this.factionMembers.computeIfAbsent(faction, f -> new HashSet<>())
                .add(factionMember);
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

    public void resetFactionMembers(Faction faction) {
        this.factionMembers.put(faction, new HashSet<>());
    }

    public void addWantedEntry(WantedEntry entry) {
        this.wantedEntries.add(entry);
    }

    public void resetWantedEntries() {
        this.wantedEntries.clear();
    }

    public void trackReinforcement(Reinforcement reinforcement) {
        // remove all previous reinforcements of the same sender
        this.reinforcements.removeIf(r -> r.getSenderPlayerName().equals(reinforcement.getSenderPlayerName()));
        // add new reinforcement
        this.reinforcements.add(reinforcement);
    }

    @Getter
    @AllArgsConstructor
    public enum Countdown {

        BANDAGE("Bandage", ofMinutes(4)),
        PILL("Schmerzpille", ofSeconds(60)),
        ABSORPTION("Absorption", ofMinutes(3));

        private final String displayName;
        private final Duration duration;
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
