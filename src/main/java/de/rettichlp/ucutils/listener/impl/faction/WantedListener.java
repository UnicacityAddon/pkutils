package de.rettichlp.ucutils.listener.impl.faction;

import de.rettichlp.ucutils.common.models.WantedEntry;
import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.nameTagService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static de.rettichlp.ucutils.common.services.CommandService.COMMAND_COOLDOWN_MILLIS;
import static java.awt.Color.decode;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.BLUE;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class WantedListener implements IMessageReceiveListener {

    private static final Pattern WANTED_GIVE_PATTERN = compile("^HQ: Gesuchter: (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)\\. Grund: (?<reason>.+)$");
    private static final Pattern WANTED_GIVE_POINTS_PATTERN = compile("^HQ: (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)'s momentanes WantedLevel: (?<wantedPointAmount>\\d+)$");
    private static final Pattern WANTED_MODIFY_PATTERN = compile("^HQ: (?<rank>.+) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)s WantedPunkte verändert!$");
    private static final Pattern WANTED_MODIFY_REASON_PATTERN = compile("^HQ: Neuer Grund: (?<reason>.+) \\[(?<oldWantedPoints>\\d+) » (?<newWantedPoints>\\d+) WantedPunkte]$");
    private static final Pattern WANTED_TICKET_PATTERN = compile("^HQ: (?<rank>.+) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) ein Ticket über (?<price>\\d+)\\$ ausgestellt\\. Bestätigung ausstehend, over\\.$");
    private static final Pattern WANTED_DELETE_PATTERN = compile("^HQ: (?<rank>.+) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)(?:'s)* Akten gelöscht, over\\.$");
    private static final Pattern WANTED_KILL_PATTERN = compile("^HQ: (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) wurde von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) getötet\\.\nHQ: Fahndungsgrund: (?<reason>.+) \\| Fahndungszeit: (?<time>.+)\\.$");
    private static final Pattern WANTED_ARREST_PATTERN = compile("^HQ: (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) wurde von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) eingesperrt\\.\nHQ: Fahndungsgrund: (?<reason>.+) \\| Fahndungszeit: (?<time>.+)\\.$");
    private static final Pattern WANTED_UNARREST_PATTERN = compile("^HQ: (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) aus dem Gefängnis entlassen\\.$");
    private static final Pattern CAR_CHECK_PATTERN = compile("^HQ: Das Fahrzeug mit dem Kennzeichen (?<plate>[A-Z0-9-]+) ist auf den Spieler (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) registriert, over\\.$");
    private static final Pattern CAR_CHECK_UNREGISTERED_PATTERN = compile("^HQ: Das Fahrzeug ist nicht registriert, over\\.$");
    private static final Pattern CAR_PARKTICKET_PATTERN = compile("^HQ: Officer (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat ein Strafzettel an das Fahrzeug \\[(?<plate>[A-Z0-9-]*)] vergeben\\.$");
    private static final Pattern CAR_PARKTICKET_REMOVE_PATTERN = compile("^HQ: (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat ein Strafzettel von dem Fahrzeug \\[(?<plate>[A-Z0-9-]*)] entfernt\\.$");
    private static final Pattern CAR_PARKTICKET_PRESENT_PATTERN = compile("^HQ: Das Fahrzeug hat einen Strafzettel aufgrund von (?<reason>.+) in Höhe von (?<price>\\d+)\\$, over\\.$");
    private static final Pattern SEARCH_TRUNK_PATTERN = compile("^Du hast den Kofferraum vom Fahrzeug \"(?<plate>[A-Z0-9-]*)\" durchsucht\\.$");
    private static final Pattern WANTED_LIST_HEADER_PATTERN = compile("Online Spieler mit WantedPunkten:");
    private static final Pattern WANTED_LIST_ENTRY_PATTERN = compile("- (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) \\| (?<wantedPointAmount>\\d+) WPS \\((?<reason>.+)\\)(?<afk> \\| AFK|)");
    private static final Pattern LICENSE_DRIVING_GIVE_PATTERN = compile("^(Agent|Agentin|Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)(?:'s)* Führerschein zurückgegeben\\.$");
    private static final Pattern LICENSE_DRIVING_TAKE_PATTERN = compile("^(Agent|Agentin|Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)(?:'s)* Führerschein abgenommen\\.$");
    private static final Pattern LICENSE_GUN_GIVE_PATTERN = compile("^(Agent|Agentin|Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)(?:'s)* Waffenschein zurückgegeben\\.$");
    private static final Pattern LICENSE_GUN_TAKE_PATTERN = compile("^(Agent|Agentin|Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+)(?:'s)* Waffenschein abgenommen\\.$");
    private static final Pattern TAKE_GUNS_PATTERN = compile("^(Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) die Waffen abgenommen\\.$");
    private static final Pattern TAKE_DRUGS_PATTERN = compile("^(Beamter|Beamtin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) (seine|ihre) Drogen abgenommen.$");
    private static final Pattern TRACKER_AGENT_PATTERN = compile("^HQ: (Agent|Agentin) (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat ein Peilsender an (?:\\[UC])?(?<targetName>[a-zA-Z0-9_]+) befestigt, over\\.$");
    private static final Pattern ROB_PATTERN = compile("^HQ: Achtung! Es wurde ein Raubüberfall gemeldet\\. Ort: (?<location>.+)\\.$");

    private static final int COLOR_PRIMARY = decode("#4498DB").getRGB();
    private static final int COLOR_SECONDARY = decode("#C8E7FF").getRGB();

    private static final Component HQ_PREFIX = empty()
            .append(literal("[").withColor(DARK_GRAY))
            .append(literal("L").withColor(COLOR_SECONDARY))
            .append(literal("e").withColor(decode("#B9DEFB").getRGB()))
            .append(literal("i").withColor(decode("#ABD5F7").getRGB()))
            .append(literal("t").withColor(decode("#9CCDF3").getRGB()))
            .append(literal("s").withColor(decode("#8DC4EF").getRGB()))
            .append(literal("t").withColor(decode("#7FBBEB").getRGB()))
            .append(literal("e").withColor(decode("#70B2E7").getRGB()))
            .append(literal("l").withColor(decode("#61AAE3").getRGB()))
            .append(literal("l").withColor(decode("#53A1DF").getRGB()))
            .append(literal("e").withColor(COLOR_PRIMARY))
            .append(literal("]").withColor(DARK_GRAY));

    private static final HeadQuarterNoBodyMessageConsumer<String, String, String> HQ_NOBODY_MESSAGE = (type, action, hoverMessage) -> empty().withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(literal(hoverMessage).withColor(COLOR_SECONDARY))))
            .append(HQ_PREFIX.copy()).append(SPACE)
            .append(literal(type).withColor(RED).withStyle(BOLD))
            .append(literal(":").withColor(COLOR_SECONDARY)).append(SPACE)
            .append(literal(action).withColor(COLOR_SECONDARY));

    private static final HeadQuarterSingleMessageConsumer<String, String, String, String> HQ_SINGLE_MESSAGE = (type, player, action, hoverMessage) -> empty().withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(literal(hoverMessage).withColor(COLOR_SECONDARY))))
            .append(HQ_PREFIX.copy()).append(SPACE)
            .append(literal(type).withColor(RED).withStyle(BOLD))
            .append(literal(":").withColor(COLOR_SECONDARY)).append(SPACE)
            .append(literal(player).withColor(COLOR_PRIMARY)).append(SPACE)
            .append(literal(!action.isBlank() ? "(" : "").withColor(COLOR_SECONDARY))
            .append(literal(action).withColor(COLOR_PRIMARY))
            .append(literal(!action.isBlank() ? ")" : "").withColor(COLOR_SECONDARY));

    private static final HeadQuarterMultiMessageConsumer<String, String, String, String, String> HQ_MULTI_MESSAGE = (type, player, target, action, hoverMessage) -> empty().withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(literal(hoverMessage).withColor(COLOR_SECONDARY))))
            .append(HQ_PREFIX.copy()).append(SPACE)
            .append(literal(type).withColor(RED).withStyle(BOLD))
            .append(literal(":").withColor(COLOR_SECONDARY)).append(SPACE)
            .append(literal(player).withColor(COLOR_PRIMARY)).append(SPACE)
            .append(literal("→").withColor(COLOR_SECONDARY)).append(SPACE)
            .append(literal(target).withColor(COLOR_PRIMARY)).append(SPACE)
            .append(literal(!action.isBlank() ? "(" : "").withColor(COLOR_SECONDARY))
            .append(literal(action).withColor(COLOR_PRIMARY))
            .append(literal(!action.isBlank() ? ")" : "").withColor(COLOR_SECONDARY));

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher wantedGiveMatcher = WANTED_GIVE_PATTERN.matcher(message);
        if (wantedGiveMatcher.find()) {
            String targetName = wantedGiveMatcher.group("targetName");
            String reason = wantedGiveMatcher.group("reason");

            storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(targetName))
                    .findFirst()
                    .ifPresent(wantedEntry -> wantedEntry.setReason(reason));

            Component component = HQ_SINGLE_MESSAGE.create("Gesucht", targetName, "", "Fahndungsgrund: " + reason);
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedGivePointsMatcher = WANTED_GIVE_POINTS_PATTERN.matcher(message);
        if (wantedGivePointsMatcher.find()) {
            String targetName = wantedGivePointsMatcher.group("targetName");
            int wantedPoints = parseInt(wantedGivePointsMatcher.group("wantedPointAmount"));

            storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(targetName))
                    .findFirst()
                    .ifPresentOrElse(wantedEntry -> wantedEntry.setWantedPointAmount(wantedPoints), () -> {
                        WantedEntry wantedEntry = new WantedEntry(targetName, wantedPoints, "");
                        storage.getWantedEntries().add(wantedEntry);
                    });

            Component component = empty()
                    .append(literal("➥").withColor(COLOR_SECONDARY)).append(SPACE)
                    .append(literal("Wantedpunkte").withColor(COLOR_PRIMARY))
                    .append(literal(":").withColor(COLOR_SECONDARY)).append(SPACE)
                    .append(literal(valueOf(wantedPoints)).withColor(COLOR_PRIMARY));

            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedModifyMatcher = WANTED_MODIFY_PATTERN.matcher(message);
        if (wantedModifyMatcher.find()) {
            String playerName = wantedModifyMatcher.group("playerName");
            String targetName = wantedModifyMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Verändert", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedModifyReasonMatcher = WANTED_MODIFY_REASON_PATTERN.matcher(message);
        if (wantedModifyReasonMatcher.find()) {
            String reason = wantedModifyReasonMatcher.group("reason");
            String oldWantedPoints = wantedModifyReasonMatcher.group("oldWantedPoints");
            String newWantedPoints = wantedModifyReasonMatcher.group("newWantedPoints");

            Component component = empty().withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(literal("Fahndungsgrund: " + reason).withColor(COLOR_SECONDARY))))
                    .append(literal("➥").withColor(COLOR_SECONDARY)).append(SPACE)
                    .append(literal(oldWantedPoints).withColor(RED)).append(SPACE)
                    .append(literal("→").withColor(COLOR_SECONDARY)).append(SPACE)
                    .append(literal(newWantedPoints).withColor(RED));

            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedTicketMatcher = WANTED_TICKET_PATTERN.matcher(message);
        if (wantedTicketMatcher.find()) {
            String playerName = wantedTicketMatcher.group("playerName");
            String targetName = wantedTicketMatcher.group("targetName");
            String price = wantedTicketMatcher.group("price");

            Component component = HQ_MULTI_MESSAGE.create("Ticket", playerName, targetName, price + "$", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedDeleteMatcher = WANTED_DELETE_PATTERN.matcher(message);
        if (wantedDeleteMatcher.find()) {
            String playerName = wantedDeleteMatcher.group("playerName");
            String targetName = wantedDeleteMatcher.group("targetName");
            int wpAmount = getWpAmountAndDelete(targetName);

            Component component = HQ_MULTI_MESSAGE.create("Gelöscht", playerName, targetName, valueOf(wpAmount), "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedKillMatcher = WANTED_KILL_PATTERN.matcher(message);
        if (wantedKillMatcher.find()) {
            String playerName = wantedKillMatcher.group("playerName");
            String targetName = wantedKillMatcher.group("targetName");
            String reason = wantedKillMatcher.group("reason");
            String time = wantedKillMatcher.group("time");
            int wpAmount = getWpAmountAndDelete(targetName);

            Component component = HQ_MULTI_MESSAGE.create("Getötet", playerName, targetName, "", "Fahndungsgrund: " + reason + "\nFahndungspunkte: " + wpAmount + "\nFahndungszeit: " + time);
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedArrestMatcher = WANTED_ARREST_PATTERN.matcher(message);
        if (wantedArrestMatcher.find()) {
            String playerName = wantedArrestMatcher.group("playerName");
            String targetName = wantedArrestMatcher.group("targetName");
            String reason = wantedArrestMatcher.group("reason");
            String time = wantedArrestMatcher.group("time");
            int wpAmount = getWpAmountAndDelete(targetName);

            Component component = HQ_MULTI_MESSAGE.create("Eingesperrt", playerName, targetName, "", "Fahndungsgrund: " + reason + "\nFahndungspunkte: " + wpAmount + "\nFahndungszeit: " + time);
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedUnarrestMatcher = WANTED_UNARREST_PATTERN.matcher(message);
        if (wantedUnarrestMatcher.find()) {
            String playerName = wantedUnarrestMatcher.group("playerName");
            String targetName = wantedUnarrestMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Entlassen", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher carCheckMatcher = CAR_CHECK_PATTERN.matcher(message);
        if (carCheckMatcher.find()) {
            String targetName = carCheckMatcher.group("targetName");
            String plate = carCheckMatcher.group("plate");

            utilService.delayedAction(() -> commandService.sendCommand("memberinfo " + targetName), COMMAND_COOLDOWN_MILLIS);

            Component component = HQ_SINGLE_MESSAGE.create("Fahrzeug", targetName, plate, "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher carCheckUnregisteredMatcher = CAR_CHECK_UNREGISTERED_PATTERN.matcher(message);
        if (carCheckUnregisteredMatcher.find()) {
            Component component = HQ_NOBODY_MESSAGE.create("Fahrzeug", "Das Fahrzeug ist nicht registriert.", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher carParkticketMatcher = CAR_PARKTICKET_PATTERN.matcher(message);
        if (carParkticketMatcher.find()) {
            String playerName = carParkticketMatcher.group("playerName");
            String plate = carParkticketMatcher.group("plate");

            Component component = HQ_SINGLE_MESSAGE.create("Strafzettel", playerName, "Gegeben an: " + (plate.isBlank() ? "nicht registriert" : plate), "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher carParkticketRemoveMatcher = CAR_PARKTICKET_REMOVE_PATTERN.matcher(message);
        if (carParkticketRemoveMatcher.find()) {
            String playerName = carParkticketRemoveMatcher.group("playerName");
            String plate = carParkticketRemoveMatcher.group("plate");

            Component component = HQ_SINGLE_MESSAGE.create("Strafzettel", playerName, "Entfernt von: " + (plate.isBlank() ? "nicht registriert" : plate), "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher carParkticketPresentMatcher = CAR_PARKTICKET_PRESENT_PATTERN.matcher(message);
        if (carParkticketPresentMatcher.find()) {
            String reason = carParkticketPresentMatcher.group("reason");
            String price = carParkticketPresentMatcher.group("price");

            Component component = HQ_NOBODY_MESSAGE.create("Strafzettel", "Grund: " + reason + ", Betrag: " + price + "$", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher searchTrunkMatcher = SEARCH_TRUNK_PATTERN.matcher(message);
        if (searchTrunkMatcher.find()) {
            String plate = searchTrunkMatcher.group("plate");

            Component component = HQ_NOBODY_MESSAGE.create("Fahrzeug", "Kofferraum durchsucht von \"" + plate + "\"", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher wantedListHeaderMatcher = WANTED_LIST_HEADER_PATTERN.matcher(message);
        if (wantedListHeaderMatcher.find()) {
            storage.getWantedEntries().clear();
            return commandService.showCommandOutputMessage("wanteds");
        }

        Matcher wantedListEntryMatcher = WANTED_LIST_ENTRY_PATTERN.matcher(message);
        if (wantedListEntryMatcher.find()) {
            String playerName = wantedListEntryMatcher.group("playerName");
            int wantedPointAmount = parseInt(wantedListEntryMatcher.group("wantedPointAmount"));
            String reason = wantedListEntryMatcher.group("reason");
            boolean isAfk = wantedListEntryMatcher.group("afk").contains("AFK");

            WantedEntry wantedEntry = new WantedEntry(playerName, wantedPointAmount, reason);
            storage.getWantedEntries().add(wantedEntry);

            TextColor color = nameTagService.getWantedPointColor(wantedPointAmount);

            if (commandService.showCommandOutputMessage("wanteds")) {
                Component modifiedMessage = empty()
                        .append(literal("➥").withColor(GRAY)).append(SPACE)
                        .append(literal(playerName).withColor(color)).append(SPACE)
                        .append(literal("-").withColor(GRAY)).append(SPACE)
                        .append(literal(reason).withColor(color)).append(SPACE)
                        .append(literal("(").withColor(GRAY))
                        .append(literal(valueOf(wantedPointAmount)).withColor(BLUE))
                        .append(literal(")").withColor(GRAY)).append(SPACE)
                        .append(literal(isAfk ? "|" : "").withColor(DARK_GRAY)).append(SPACE)
                        .append(literal(isAfk ? "AFK" : "").withColor(GRAY));

                player.sendSystemMessage(modifiedMessage);
            }

            return false;
        }

        Matcher licenseDrivingGiveMatcher = LICENSE_DRIVING_GIVE_PATTERN.matcher(message);
        if (licenseDrivingGiveMatcher.find()) {
            String playerName = licenseDrivingGiveMatcher.group("playerName");
            String targetName = licenseDrivingGiveMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Führerscheinrückgabe", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher licenseDrivingTakeMatcher = LICENSE_DRIVING_TAKE_PATTERN.matcher(message);
        if (licenseDrivingTakeMatcher.find()) {
            String playerName = licenseDrivingTakeMatcher.group("playerName");
            String targetName = licenseDrivingTakeMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Führerscheinabnahme", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher licenseGunGiveMatcher = LICENSE_GUN_GIVE_PATTERN.matcher(message);
        if (licenseGunGiveMatcher.find()) {
            String playerName = licenseGunGiveMatcher.group("playerName");
            String targetName = licenseGunGiveMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Waffenscheinrückgabe", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher licenseGunTakeMatcher = LICENSE_GUN_TAKE_PATTERN.matcher(message);
        if (licenseGunTakeMatcher.find()) {
            String playerName = licenseGunTakeMatcher.group("playerName");
            String targetName = licenseGunTakeMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Waffenscheinabnahme", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher takeGunsMatcher = TAKE_GUNS_PATTERN.matcher(message);
        if (takeGunsMatcher.find()) {
            String playerName = takeGunsMatcher.group("playerName");
            String targetName = takeGunsMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Waffenabnahme", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher takeDrugsMatcher = TAKE_DRUGS_PATTERN.matcher(message);
        if (takeDrugsMatcher.find()) {
            String playerName = takeDrugsMatcher.group("playerName");
            String targetName = takeDrugsMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Drogenabnahme", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher trackerMatcher = TRACKER_AGENT_PATTERN.matcher(message);
        if (trackerMatcher.find()) {
            String playerName = trackerMatcher.group("playerName");
            String targetName = trackerMatcher.group("targetName");

            Component component = HQ_MULTI_MESSAGE.create("Peilsender", playerName, targetName, "", "");
            player.sendSystemMessage(component);
            return false;
        }

        Matcher robMatcher = ROB_PATTERN.matcher(message);
        if (robMatcher.find()) {
            String location = robMatcher.group("location");

            Component component = HQ_NOBODY_MESSAGE.create("Raubüberfall", "Es wurde ein Raubüberfall bei " + location + " gemeldet.", "");
            player.sendSystemMessage(component);
            return false;
        }

        return true;
    }

    private int getWpAmountAndDelete(String targetName) {
        Predicate<WantedEntry> predicate = wantedEntry -> wantedEntry.getPlayerName().equals(targetName);
        int wantedPointAmount = storage.getWantedEntries().stream()
                .filter(predicate)
                .findAny()
                .map(WantedEntry::getWantedPointAmount)
                .orElse(0);

        storage.getWantedEntries().removeIf(predicate);
        return wantedPointAmount;
    }

    @FunctionalInterface
    private interface HeadQuarterNoBodyMessageConsumer<Type, Action, HoverMessage> {

        Component create(String type, String action, String hoverMessage);
    }

    @FunctionalInterface
    private interface HeadQuarterSingleMessageConsumer<Type, Player, Action, HoverMessage> {

        Component create(String type, String player, String action, String hoverMessage);
    }

    @FunctionalInterface
    private interface HeadQuarterMultiMessageConsumer<Type, Player, Target, Action, HoverMessage> {

        Component create(String type, String player, String target, String action, String hoverMessage);
    }
}
