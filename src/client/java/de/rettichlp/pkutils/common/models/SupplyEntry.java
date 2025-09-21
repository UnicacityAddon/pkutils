package de.rettichlp.pkutils.common.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data @RequiredArgsConstructor
public class SupplyEntry {
    private final String drug;
    private final int purity;
    private final int amount;
}
