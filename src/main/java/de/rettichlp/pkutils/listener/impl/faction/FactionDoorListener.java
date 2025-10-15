package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IBlockRightClickListener;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Faction.FBI;
import static de.rettichlp.pkutils.common.models.Faction.KERZAKOV;
import static de.rettichlp.pkutils.common.models.Faction.WESTSIDEBALLAS;
import static java.util.Collections.emptySet;
import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.Hand.OFF_HAND;

@PKUtilsListener
public class FactionDoorListener extends PKUtilsBase implements IBlockRightClickListener {

    private static final Map<Faction, Set<BlockPos>> FACTION_DOOR_POSITIONS = Map.of(
            FBI, Set.of(new BlockPos(879, 62, -87)),
            KERZAKOV, Set.of(new BlockPos(936, 69, 191), new BlockPos(936, 69, 174)),
            WESTSIDEBALLAS, Set.of(new BlockPos(-166, 68, 205)));
    private static final int DISTANCE = 4;

    @Override
    public void onBlockRightClick(World world, Hand hand, BlockHitResult hitResult) {
        if (hand == OFF_HAND || !player.getStackInHand(MAIN_HAND).isEmpty()) {
            return;
        }

        Faction faction = storage.getFaction(player.getGameProfile().getName());
        Set<BlockPos> factionDoorPositions = FACTION_DOOR_POSITIONS.getOrDefault(faction, emptySet());

        factionDoorPositions.stream()
                .filter(blockPos -> blockPos.isWithinDistance(hitResult.getBlockPos(), DISTANCE))
                .findAny()
                .ifPresent(blockPos -> sendCommand("fdoor"));
    }
}
