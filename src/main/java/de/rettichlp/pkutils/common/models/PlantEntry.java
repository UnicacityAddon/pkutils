package de.rettichlp.pkutils.common.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.BlockPos;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class PlantEntry {

    private final BlockPos blockPos;
    private final LocalDateTime plantedAt;
    private LocalDateTime lastWateredAt;
    private LocalDateTime lastFertilizedAt;
}
