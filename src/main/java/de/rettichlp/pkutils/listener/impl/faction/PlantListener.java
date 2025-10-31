package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.PlantEntry;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IBlockRightClickListener;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static net.minecraft.block.Blocks.FERN;
import static net.minecraft.block.Blocks.PODZOL;
import static net.minecraft.item.Items.BONE_MEAL;
import static net.minecraft.item.Items.PUMPKIN_SEEDS;
import static net.minecraft.item.Items.WATER_BUCKET;
import static net.minecraft.item.Items.WHEAT_SEEDS;
import static net.minecraft.screen.slot.SlotActionType.PICKUP;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;

@PKUtilsListener
public class PlantListener implements IBlockRightClickListener, IEntityRenderListener, IMessageReceiveListener, IScreenOpenListener {

    private static final String PLANT_TEXT = "Plantage";
    private static final Pattern PLANT_PLANT_PATTERN = compile("^\\[Plantage] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat eine (Kräuter|Pulver)-Plantage gesetzt\\. \\[\\d+/10]$");
    private static final Pattern PLANT_WATER_PATTERN = compile("^\\[Plantage] Eine (Kräuter|Pulver)-Plantage wurde von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) gewässert\\.$");
    private static final Pattern PLANT_FERTILIZE_PATTERN = compile("^\\[Plantage] Eine (Kräuter|Pulver)-Plantage wurde von (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) gedüngt\\.$");

    private static final int PLANT_WATERING_INTERVAL_MINUTES = 40;
    private static final int PLANT_FERTILIZING_INTERVAL_MINUTES = 45;

    @Override
    public void onBlockRightClick(World world, Hand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();

        boolean targetBlockIsPlant = player.getWorld().getBlockState(blockPos).getBlock().equals(FERN) && player.getWorld().getBlockState(blockPos.down()).getBlock().equals(PODZOL);
        if (!targetBlockIsPlant) {
            // check for plant placing
            ItemStack mainHandStack = player.getInventory().getMainHandStack();

            if (player.isSneaking() && (mainHandStack.isOf(PUMPKIN_SEEDS) || mainHandStack.isOf(WHEAT_SEEDS))) {
                commandService.sendCommand("plant plant");
            }

            return;
        }

        boolean isStandingOnPlant = player.getBlockPos().equals(blockPos);
        if (!isStandingOnPlant) {
            messageService.sendModMessage("Du musst auf der Plantage stehen, um sie via PKUtils zu verwalten.", false);
            return;
        }

        commandService.sendCommand("plant");
    }

    @Override
    public void onEntityRender(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vertexConsumers = context.consumers();
        ClientWorld world = MinecraftClient.getInstance().world;

        if (nonNull(matrices) && nonNull(vertexConsumers) && nonNull(world)) {
            // create a copy to avoid ConcurrentModificationException
            new ArrayList<>(storage.getPlantEntries()).forEach(plantEntry -> {
                Vec3d centerBlockPos = plantEntry.getBlockPos().toCenterPos();
                double x = centerBlockPos.x;
                double y = centerBlockPos.y;
                double z = centerBlockPos.z;

                // only render if the player is nearby
                if (!player.getBlockPos().isWithinDistance(plantEntry.getBlockPos(), 15)) {
                    return;
                }

                Text waterText;
                LocalDateTime lastWateredAt = plantEntry.getLastWateredAt();
                if (nonNull(lastWateredAt)) {
                    LocalDateTime nextWateringAt = lastWateredAt.plusMinutes(PLANT_WATERING_INTERVAL_MINUTES);
                    Duration durationUntilNextWatering = between(now(), nextWateringAt);
                    long millis = durationUntilNextWatering.toMillis();

                    waterText = empty()
                            .append(of("🫗").copy().formatted(BLUE)).append(" ")
                            .append(of(messageService.millisToFriendlyString(millis)).copy().formatted(getTextColor(millis)));
                } else {
                    waterText = of("↓").copy().formatted(AQUA);
                }

                Text fertilizeText;
                LocalDateTime lastFertilizedAt = plantEntry.getLastFertilizedAt();
                if (nonNull(lastFertilizedAt)) {
                    LocalDateTime nextFertilizingAt = lastFertilizedAt.plusMinutes(PLANT_FERTILIZING_INTERVAL_MINUTES);
                    Duration durationUntilNextFertilizing = between(now(), nextFertilizingAt);
                    long millis = durationUntilNextFertilizing.toMillis();

                    fertilizeText = empty()
                            .append(of("🫘").copy().formatted(GOLD)).append(" ")
                            .append(of(messageService.millisToFriendlyString(millis)).copy().formatted(getTextColor(millis)));
                } else {
                    fertilizeText = of("↓").copy().formatted(AQUA);
                }

                renderService.renderTextAt(matrices, vertexConsumers, x, y + 0.75, z, waterText, 0.015f);
                renderService.renderTextAt(matrices, vertexConsumers, x, y + 0.6, z, fertilizeText, 0.015f);
            });
        }
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher plantPlantMatcher = PLANT_PLANT_PATTERN.matcher(message);
        if (plantPlantMatcher.find() && player.getGameProfile().getName().equals(plantPlantMatcher.group("playerName"))) {
            BlockPos blockPos = player.getBlockPos();

            PlantEntry plantEntry = new PlantEntry(blockPos, now());
            storage.getPlantEntries().add(plantEntry);

            return true;
        }

        Matcher plantFertilizeMatcher = PLANT_FERTILIZE_PATTERN.matcher(message);
        if (plantFertilizeMatcher.find()) {
            BlockPos blockPos = player.getBlockPos();

            storage.getPlantEntries().stream()
                    .filter(plantEntry -> plantEntry.getBlockPos().equals(blockPos))
                    .findAny().ifPresent(plantEntry -> plantEntry.setLastFertilizedAt(now()));

            return true;
        }

        Matcher plantWaterMatcher = PLANT_WATER_PATTERN.matcher(message);
        if (plantWaterMatcher.find()) {
            BlockPos blockPos = player.getBlockPos();

            storage.getPlantEntries().stream()
                    .filter(plantEntry -> plantEntry.getBlockPos().equals(blockPos))
                    .findAny().ifPresent(plantEntry -> plantEntry.setLastWateredAt(now()));

            return true;
        }

        return true;
    }

    @Override
    public void onScreenOpen(Screen screen, int scaledWidth, int scaledHeight) {
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (nonNull(interactionManager) && screen instanceof GenericContainerScreen genericContainerScreen && PLANT_TEXT.equals(genericContainerScreen.getTitle().getString())) {
            ItemStack mainHandStack = player.getInventory().getMainHandStack();

            int syncId = genericContainerScreen.getScreenHandler().syncId;
            if (mainHandStack.isOf(WATER_BUCKET)) {
                interactionManager.clickSlot(syncId, 1, 0, PICKUP, player);
            } else if (mainHandStack.isOf(BONE_MEAL)) {
                interactionManager.clickSlot(syncId, 7, 0, PICKUP, player);
            }
        }
    }

    private Formatting getTextColor(long millis) {
        if (millis < 0) {
            return RED;
        } else if (millis <= 3 * 60 * 1000) {
            return GREEN;
        } else {
            return GRAY;
        }
    }
}
