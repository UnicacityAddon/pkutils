package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.listener.IClickListener;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static de.rettichlp.pkutils.PKUtilsClient.*;

public class FactionDoorListener implements IClickListener {

    public List<BlockPos> doorPositions = List.of(
            new BlockPos(-166, 68, 205), //Ballas
            new BlockPos(936, 69, 191), //Kerzakov rechts
            new BlockPos(936, 69, 174), //Kerzakov links
            new BlockPos(879, 62, -87)); //FBI

    @Override
    public void onClick() {
        if (player.getWorld() == null) return;

        if (!isInFaction(Faction.FBI) || !isInFaction(Faction.WESTSIDEBALLAS) || !isInFaction(Faction.KERZAKOV)) {
            return;
        }

        BlockPos clickedBlockPos = player.getBlockPos().offset(player.getHorizontalFacing());
        int distance = 4;

        for (BlockPos doorPosition : doorPositions) {
            if (clickedBlockPos.isWithinDistance(doorPosition, distance)) {
                networkHandler.sendCommand("/fdoor");
            }
        }
    }

    private boolean isInFaction(Faction faction) {
        return storage.getFaction(player.getName().toString()).equals(faction);
    }
}
