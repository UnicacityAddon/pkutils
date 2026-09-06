package de.rettichlp.ucutils.common;

import de.rettichlp.ucutils.common.models.Countdown;
import de.rettichlp.ucutils.common.models.Faction;
import de.rettichlp.ucutils.common.models.FactionEntry;
import de.rettichlp.ucutils.common.models.FactionMember;
import de.rettichlp.ucutils.common.models.Job;
import de.rettichlp.ucutils.common.models.ShutdownReason;
import de.rettichlp.ucutils.common.models.TeamResponse;
import de.rettichlp.ucutils.common.models.WantedEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.rettichlp.ucutils.UCUtils.LOGGER;
import static de.rettichlp.ucutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.ucutils.common.models.Faction.NULL;
import static net.minecraft.network.chat.Component.translatable;

public class Storage {

    @Getter
    private final List<ShutdownReason> activeShutdowns = new ArrayList<>();

    @Getter
    private final List<Countdown> countdowns = new ArrayList<>();

    @Getter
    private final Set<FactionEntry> factionEntries = new HashSet<>();

    @Getter
    private final Map<String, LocalDateTime> medicBandageCooldowns = new HashMap<>();

    @Getter
    private final Map<String, LocalDateTime> medicPillCooldowns = new HashMap<>();

    @Getter
    private final Map<String, Faction> playerFactionCache = new HashMap<>();

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    @Getter
    @Setter
    private int activeServices = 0;

    @Getter
    @Setter
    private String lastRelevantReinforcementSenderName = "";

    @Getter
    @Setter
    @Nullable
    private Vec3 blackMarketPosition;

    @Getter
    @Setter
    @Nullable
    private Vec3 bloodDealerPosition;

    @Getter
    @Setter
    @Nullable
    private MapId captchaMap;

    @Getter
    @Setter
    private boolean carLocked = true;

    @Getter
    @Setter
    @Nullable
    private Job currentJob;

    @Getter
    @Setter
    private boolean dead = false;

    @Getter
    @Setter
    @Nullable
    private Vec3 dealerPosition;

    @Getter
    @Setter
    private double hydration = -1.0;

    @Getter
    @Setter
    private long joinTimestamp = 0;

    @Getter
    @Setter
    private int medicStatus = -1;

    @Getter
    @Setter
    private Minecart minecartEntityToHighlight;

    @Getter
    @Setter
    private int moneyAtmAmount = 0;

    @Getter
    @Setter
    private boolean premium = false;

    @Getter
    @Setter
    private boolean stockMarketCommandRunning = false;

    @Getter
    @Setter
    @Nullable
    private Vec3 summerTreasurePosition;

    @Getter
    @Setter
    private TeamResponse team;

    @Getter
    @Setter
    private ToggledChat toggledChat = NONE;

    @Getter
    @Setter
    private boolean unicaCity = false;

    public void print() {
        //activeShutdowns
        LOGGER.info("activeShutdowns[{}]: {}", this.activeShutdowns.size(), this.activeShutdowns);
        // blackMarketPosition
        LOGGER.info("blackMarketPosition: {}", this.blackMarketPosition);
        // bloodDealerPosition
        LOGGER.info("bloodDealerPosition: {}", this.bloodDealerPosition);
        // captchaMapImage
        LOGGER.info("captchaMap: {}", this.captchaMap);
        // countdowns
        LOGGER.info("countdowns[{}]: {}", this.countdowns.size(), this.countdowns);
        // dealerPosition
        LOGGER.info("dealerPosition: {}", this.dealerPosition);
        // factionEntries
        this.factionEntries.forEach(factionEntry -> LOGGER.info("factionEntries[{}:{}]: {}", factionEntry.faction(), factionEntry.members().size(), factionEntry.members()));
        // medicBandageCooldowns
        LOGGER.info("medicBandageCooldowns[{}]: {}", this.medicBandageCooldowns.size(), this.medicBandageCooldowns);
        // medicPillCooldowns
        LOGGER.info("medicPillCooldowns[{}]: {}", this.medicPillCooldowns.size(), this.medicPillCooldowns);
        // playerFactionCache
        LOGGER.info("playerFactionCache[{}]: {}", this.playerFactionCache.size(), this.playerFactionCache);
        // wantedEntries
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
        // activeServices
        LOGGER.info("activeServices: {}", this.activeServices);
        // lastRelevantReinforcementSenderName
        LOGGER.info("lastRelevantReinforcementSenderName: {}", this.lastRelevantReinforcementSenderName);
        // carLocked
        LOGGER.info("carLocked: {}", this.carLocked);
        // currentJob
        LOGGER.info("currentJob: {}", this.currentJob);
        // dead
        LOGGER.info("dead: {}", this.dead);
        // hydration
        LOGGER.info("hydration: {}", this.hydration);
        // joinTimestamp
        LOGGER.info("joinTimestamp: {}", this.joinTimestamp);
        // medicStatus
        LOGGER.info("medicStatus: {}", this.medicStatus);
        // minecartEntityToHighlight
        LOGGER.info("minecartEntityToHighlight: {}", this.minecartEntityToHighlight);
        // moneyAtmAmount
        LOGGER.info("moneyAtmAmount: {}", this.moneyAtmAmount);
        // premium
        LOGGER.info("premium: {}", this.premium);
        // stockMarketCommandRunning
        LOGGER.info("stockMarketCommandRunning: {}", this.stockMarketCommandRunning);
        // summerTreasurePosition
        LOGGER.info("summerTreasurePosition: {}", this.summerTreasurePosition);
        // team
        LOGGER.info("team: {}", this.team);
        // toggledChat
        LOGGER.info("toggledChat: {}", this.toggledChat);
        // unicaCity
        LOGGER.info("unicaCity: {}", this.unicaCity);
    }

    public Faction getFaction(String playerName) {
        Faction cachedFaction = this.playerFactionCache.get(playerName);
        if (cachedFaction != null) {
            return cachedFaction;
        }

        Faction faction = this.factionEntries.stream()
                .filter(factionEntry -> factionEntry.members().stream()
                        .anyMatch(factionMember -> factionMember.username().equalsIgnoreCase(playerName)))
                .findFirst()
                .map(FactionEntry::faction)
                .orElse(NULL);

        this.playerFactionCache.put(playerName, faction);
        return faction;
    }

    public Optional<FactionMember> getFactionMember(String playerName) {
        return this.factionEntries.stream()
                .flatMap(factionEntry -> factionEntry.members().stream())
                .filter(factionMember -> factionMember.username().equals(playerName))
                .findFirst();
    }

    @Getter
    @AllArgsConstructor
    public enum ToggledChat {

        NONE("", translatable("ucutils.notification.toggled_chat.none")),
        D_CHAT("d", translatable("ucutils.notification.toggled_chat.d")),
        F_CHAT("f", translatable("ucutils.notification.toggled_chat.f")),
        W_CHAT("w", translatable("ucutils.notification.toggled_chat.w"));

        private final String command;
        private final Component toggleMessage;
    }
}
