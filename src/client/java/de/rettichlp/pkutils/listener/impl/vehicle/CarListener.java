package de.rettichlp.pkutils.listener.impl.vehicle;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR;

@PKUtilsListener
public class CarListener extends PKUtilsBase implements IMessageReceiveListener, ITickListener {

    private static final Pattern CAR_UNLOCK_PATTERN = compile("^\\[Car] Du hast deinen .+ aufgeschlossen\\.$");

    private boolean carUnlocked = false;
    private boolean waitingToClickLock = false;

    @Override
    public boolean onMessageReceive(Text text, String message) {
        if (!storage.isCarLock()) {
            return true;
        }

        Matcher carUnlockMatcher = CAR_UNLOCK_PATTERN.matcher(message);
        if (carUnlockMatcher.find()) {
            this.carUnlocked = true;
        }

        return true;
    }

    @Override
    public void onTick() {
        if (!storage.isCarLock()) {
            return;
        }

        // Phase 1: Player unlocked car and is now inside it
        if (this.carUnlocked && isCarScoreboardVisible()) {
            networkHandler.sendChatCommand("car lock");
            this.carUnlocked = false;
            this.waitingToClickLock = true;
        }

        // Phase 2: The /car lock GUI is open, now click the emerald
        if (this.waitingToClickLock) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof GenericContainerScreen screen && screen.getTitle().getString().equals("CarControl")) {
                // The emerald is in the first slot (index 0)
                client.interactionManager.clickSlot(screen.getScreenHandler().syncId, 0, 0, SlotActionType.PICKUP, player);
                this.waitingToClickLock = false;
                hudService.sendInfoNotification("Fahrzeug automatisch verriegelt.");
            }
        }
    }

    private boolean isCarScoreboardVisible() {
        return getCarScoreboard().isPresent();
    }

    private Optional<ScoreboardObjective> getCarScoreboard() {
        assert MinecraftClient.getInstance().world != null;
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(SIDEBAR);

        if (nonNull(scoreboardObjective)) {
            // We check for a specific line on the scoreboard that only appears when in a car.
            boolean hasZustand = scoreboard.getScoreboardEntries(scoreboardObjective).stream() // KORRIGIERTE ZEILE
                    .anyMatch(entry -> entry.name().getString().contains("Zustand"));

            if (hasZustand) {
                return Optional.of(scoreboardObjective);
            }
        }
        return empty();
    }
}