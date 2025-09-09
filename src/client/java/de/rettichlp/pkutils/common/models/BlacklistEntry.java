package de.rettichlp.pkutils.common.models;

public record BlacklistEntry(String playerName, String reason, boolean outlaw, int kills, int price) {

}
