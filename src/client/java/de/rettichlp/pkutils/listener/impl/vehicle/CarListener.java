package de.rettichlp.pkutils.listener.impl.vehicle;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IEnterVehicleListener;
import de.rettichlp.pkutils.listener.IScreenOpenListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR;
import static net.minecraft.screen.slot.SlotActionType.PICKUP;

@PKUtilsListener
public class CarListener extends PKUtilsBase implements IEnterVehicleListener, IMessageReceiveListener, IScreenOpenListener {

    private static final Pattern CAR_UNLOCK_PATTERN = compile("^\\[Car] Du hast deinen .+ aufgeschlossen\\.$");
    private static final Pattern CAR_LOCK_PATTERN = compile("^\\[Car] Du hast deinen .+ abgeschlossen\\.$");

    private boolean carLocked = true;

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher carUnlockMatcher = CAR_UNLOCK_PATTERN.matcher(message);
        if (carUnlockMatcher.find()) {
            this.carLocked = false;
            return true;
        }

        Matcher carLockMatcher = CAR_LOCK_PATTERN.matcher(message);
        if (carLockMatcher.find()) {
            this.carLocked = true;
            return true;
        }

        return true;
    }

    @Override
    public void onScreenOpen(Screen screen, int scaledWidth, int scaledHeight) {
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (nonNull(interactionManager) && screen instanceof GenericContainerScreen genericContainerScreen) { // TODO setting entry
            interactionManager.clickSlot(genericContainerScreen.getScreenHandler().syncId, 0, 0, PICKUP, player);
            delayedAction(() -> hudService.sendInfoNotification("Fahrzeug automatisch " + (this.carLocked ? "verriegelt" : "entriegelt")), 100);
        }
    }

    @Override
    public void onEnterVehicle(Entity vehicle) {
        if (!storage.isCarLock()) {
            return;
        }

        // the entity is a car
        if (!isCar(vehicle)) {
            return;
        }

        // start car
        networkHandler.sendChatCommand("car start");

        // lock car after 1 second if not already locked
        if (!this.carLocked) {
            delayedAction(() -> networkHandler.sendChatCommand("car lock"), 1000);
        }
    }

    private boolean isCar(Entity vehicle) {
        return vehicle instanceof MinecartEntity && isCarScoreboardVisible();
    }

    private boolean isCarScoreboardVisible() {
        assert MinecraftClient.getInstance().world != null;
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(SIDEBAR);

        // we check for a specific line on the scoreboard that only appears when in a car
        return nonNull(scoreboardObjective) && scoreboard.getScoreboardEntries(scoreboardObjective).stream()
                .anyMatch(entry -> entry.name().getString().contains("Zustand"));
    }
}
