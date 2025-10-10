package de.rettichlp.pkutils.common.models;

import lombok.Data;
import net.minecraft.util.math.BlockPos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Data
public class Reinforcement {

    private final LocalDateTime createdAt = now();
    private final List<String> acceptedPlayerNames = new ArrayList<>();
    private final String type;
    private final String senderPlayerName;
    private final String naviPoint;
    private final String distance;
    private BlockPos blockPos;
    private boolean addedAsActivity;
}
