package de.rettichlp.pkutils.common.storage.schema;

public record BlacklistEntry(String playerName, String reason, boolean outlaw, int kills, int price) {

}
