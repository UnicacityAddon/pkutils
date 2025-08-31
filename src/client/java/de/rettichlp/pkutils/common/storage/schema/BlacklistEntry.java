package de.rettichlp.pkutils.common.storage.schema;

import lombok.Data;

@Data
public class BlacklistEntry {

    private final String playerName;
    private final String reason;
    private final boolean outlaw;
    private final int kills;
    private final int price;
}
