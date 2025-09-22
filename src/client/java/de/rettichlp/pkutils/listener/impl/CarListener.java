package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IEnterVehicleListener;
import de.rettichlp.pkutils.listener.IEntityRenderListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IScreenOpenListener;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.renderService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR;
import static net.minecraft.screen.slot.SlotActionType.PICKUP;
import static net.minecraft.util.Formatting.AQUA;

@PKUtilsListener
public class CarListener extends PKUtilsBase
        implements IEnterVehicleListener, IEntityRenderListener, IMessageReceiveListener, IScreenOpenListener {

    private static final Pattern CAR_UNLOCK_PATTERN = compile("^\\[Car] Du hast deinen .+ aufgeschlossen\\.$");
    private static final Pattern CAR_LOCK_PATTERN = compile("^\\[Car] Du hast deinen .+ abgeschlossen\\.$");

    private boolean carLocked = true;

    @Override
    public void onEnterVehicle(Entity vehicle) {
        storage.setMinecartEntityToHighlight(null);

        // TODO setting entry

        // the entity is a car
        if (!isCar(vehicle)) {
            return;
        }

        // start the car with a small delay to ensure the player is fully in the vehicle
        delayedAction(() -> sendCommand("car start"), 500);

        // lock the car after 1 second and the small delay if not already locked
        if (!this.carLocked) {
            delayedAction(() -> sendCommand("car lock"), 1500);
        }
    }

    @Override
    public void onEntityRender(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vertexConsumers = context.consumers();

        if (nonNull(matrices) && nonNull(vertexConsumers)) {
            ofNullable(storage.getMinecartEntityToHighlight()).ifPresent(minecartEntity -> {
                renderService.renderTextAboveEntity(matrices, vertexConsumers, minecartEntity, Text.of("🚗").copy().formatted(AQUA), 0.05F);
            });
        }
    }

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

        if (nonNull(interactionManager) && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().equals("CarControl")) { // TODO setting entry
            interactionManager.clickSlot(genericContainerScreen.getScreenHandler().syncId, 0, 0, PICKUP, player);
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
