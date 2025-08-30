package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;

@PKUtilsListener
public class BombListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern BOMB_FOUND_PATTERN = Pattern.compile("^News: ACHTUNG! Es wurde eine Bombe in der Nähe von (?<location>.+) gefunden!$");
    private static final Pattern BOMB_STOP_PATTERN = Pattern.compile("^News: Die Bombe konnte (erfolgreich entschärft werden|nicht entschärft werden)!$");

    public static boolean bombTimerActive = false;
    public static long bombPlantedTimestamp = 0L;
    public static String bombLocation = "";

    @Override
    public boolean onMessageReceive(String message) {
        Faction playerFaction = storage.getFaction(player.getName().getString());
        if (playerFaction != Faction.POLIZEI && playerFaction != Faction.FBI && playerFaction != Faction.RETTUNGSDIENST) {
            return true; // Nur für Polizei & FBI sichtbar
        }

        Matcher bombFoundMatcher = BOMB_FOUND_PATTERN.matcher(message);
        if (bombFoundMatcher.find()) {
            bombTimerActive = true;
            bombPlantedTimestamp = System.currentTimeMillis();
            bombLocation = bombFoundMatcher.group("location");
            return true;
        }

        Matcher bombStopMatcher = BOMB_STOP_PATTERN.matcher(message);
        if (bombStopMatcher.find()) {
            bombTimerActive = false;
            bombPlantedTimestamp = 0L;
            bombLocation = "";
            return true;
        }

        return true;
    }
}