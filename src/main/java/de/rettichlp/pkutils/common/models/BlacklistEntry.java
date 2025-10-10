package de.rettichlp.pkutils.common.models;

import lombok.Getter;

@Getter
public class BlacklistEntry extends BlacklistReason {

    private final String playerName;

    public BlacklistEntry(String playerName, String reason, boolean outlaw, int kills, int price) {
        super(reason, outlaw, kills, price);
        this.playerName = playerName;
    }
}
