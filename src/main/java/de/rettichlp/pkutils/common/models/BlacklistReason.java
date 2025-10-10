package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class BlacklistReason {

    private final String reason;
    private final boolean outlaw;
    private final int kills;
    private final int price;
}
