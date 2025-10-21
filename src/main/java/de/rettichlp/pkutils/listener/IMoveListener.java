package de.rettichlp.pkutils.listener;

import net.minecraft.util.math.BlockPos;

public interface IMoveListener extends IPKUtilsListener {

    void onMove(BlockPos blockPos);
}
