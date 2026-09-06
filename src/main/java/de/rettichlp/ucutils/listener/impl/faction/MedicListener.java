package de.rettichlp.ucutils.listener.impl.faction;

import de.rettichlp.ucutils.common.models.Countdown;
import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.awt.Color;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.LOGGER;
import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static de.rettichlp.ucutils.common.services.CommandService.COMMAND_COOLDOWN_MILLIS;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.time.Duration.ofMinutes;
import static java.time.LocalDateTime.now;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.ChatFormatting.DARK_AQUA;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.DARK_GREEN;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.YELLOW;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.LIGHT_PURPLE;

@UCUtilsListener
public class MedicListener implements IMessageReceiveListener {

    public static final Duration MEDIC_BANDAGE_DURATION = ofMinutes(4);
    public static final Duration MEDIC_PILL_DURATION = ofMinutes(4);

    private static final Pattern MEDIC_BANDAGE_PATTERN = compile("^(?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat dich bandagiert\\.$");
    private static final Pattern MEDIC_BANDAGE_GIVE_PATTERN = compile("^Du hast (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) bandagiert\\.$");
    private static final Pattern MEDIC_BANDAGE_ALREADY_GIVEN_PATTERN = compile("^Der Spieler ist bereits bandagiert\\. \\(\\d+ Sekunden? verbleibend\\)$");
    private static final Pattern MEDIC_BANDAGE_ALREADY_GIVEN_SELF_PATTERN = compile("^Du bist bereits bandagiert\\. \\(\\d+ Sekunden? verbleibend\\)$");
    private static final Pattern MEDIC_PILL_PATTERN = compile("^\\[Medic] Doktor (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat dir Schmerzpillen verabreicht\\.$");
    private static final Pattern MEDIC_PILL_GIVE_PATTERN = compile("^\\[Medic] Du hast (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) Schmerzpillen verabreicht\\.$");
    private static final Pattern MEDIC_REVIVE_START_PATTERN = compile("^Du beginnst mit der Wiederbelebung von (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+)\\.\\.\\.$");
    private static final Pattern MEDIC_STATUS_PATTERN = compile("^HQ: (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+) hat den Status (?<status>[1-6]) \\(.+\\) betreten\\.$");
    private static final Pattern LABOR_TRANSPORT_STARTED_PATTERN = compile("^\\[ʟᴀʙᴏʀ] Transport gestartet: (?<chestAmount>\\d+) ᴋɪsᴛᴇɴ mit (?<ingredientAmount>\\d+) (?<ingredient>.+)$");
    private static final Pattern STORAGE_INGREDIENT_SHARE_PATTERN = compile("^.+ (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+): (?<ingredient1>\\d+)x Wirkstoff \\| (?<ingredient2>\\d+)x Trägerstoff \\| (?<ingredient3>\\d+)x Zusatzstoff$");
    private static final Pattern STORAGE_INGREDIENT_ACCEPT_PATTERN = compile("^.+ (?:\\[UC])?(?<playerName>[a-zA-Z0-9_]+): Ich nehme (?<ingredient>Wirkstoff|Trägerstoff|Zusatzstoff)! \\(geschätzt: (?<amountBefore>\\d+) → (?<amountAfter>\\d+)\\)$");

    private static final MutableComponent STORAGE_TEXT = empty()
            .append(literal("ʟ").withColor(Color.decode("#AA0000").getRGB()))
            .append(literal("ᴀ").withColor(Color.decode("#BF1515").getRGB()))
            .append(literal("ʙ").withColor(Color.decode("#D52B2B").getRGB()))
            .append(literal("ᴏ").withColor(Color.decode("#EA4040").getRGB()))
            .append(literal("ʀ").withColor(Color.decode("#FF5555").getRGB()));

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher medicBandageMatcher = MEDIC_BANDAGE_PATTERN.matcher(message);
        if (medicBandageMatcher.find()) {
            new Countdown("Bandage", MEDIC_BANDAGE_DURATION);
            return true;
        }

        Matcher medicBandageGiveMatcher = MEDIC_BANDAGE_GIVE_PATTERN.matcher(message);
        if (medicBandageGiveMatcher.find()) {
            String playerName = medicBandageGiveMatcher.group("playerName");
            storage.getMedicBandageCooldowns().put(playerName, now().plus(MEDIC_BANDAGE_DURATION));
            return true;
        }

        Matcher medicBandageAlreadyGivenMatcher = MEDIC_BANDAGE_ALREADY_GIVEN_PATTERN.matcher(message);
        if (medicBandageAlreadyGivenMatcher.find()) {
            player.sendOverlayMessage(literal(message.replace(".", ":").replace("(", "").replace(")", "")).withColor(LIGHT_PURPLE));
            return false;
        }

        Matcher medicBandageAlreadyGivenSelfMatcher = MEDIC_BANDAGE_ALREADY_GIVEN_SELF_PATTERN.matcher(message);
        if (medicBandageAlreadyGivenSelfMatcher.find()) {
            player.sendOverlayMessage(literal(message.replace(".", ":").replace("(", "").replace(")", "")).withColor(LIGHT_PURPLE));
            return false;
        }

        Matcher medicPillMatcher = MEDIC_PILL_PATTERN.matcher(message);
        if (medicPillMatcher.find()) {
            new Countdown("Schmerzpille", MEDIC_PILL_DURATION);
            return true;
        }

        Matcher medicPillGiveMatcher = MEDIC_PILL_GIVE_PATTERN.matcher(message);
        if (medicPillGiveMatcher.find()) {
            String playerName = medicPillGiveMatcher.group("playerName");
            storage.getMedicPillCooldowns().put(playerName, now().plus(MEDIC_PILL_DURATION));
            return true;
        }

        Matcher medicReviveStartMatcher = MEDIC_REVIVE_START_PATTERN.matcher(message);
        if (medicReviveStartMatcher.find()) {
            utilService.delayedAction(() -> commandService.sendCommand("dinfo"), COMMAND_COOLDOWN_MILLIS);
            return true;
        }

        Matcher medicStatusMatcher = MEDIC_STATUS_PATTERN.matcher(message);
        if (medicStatusMatcher.find()) {
            String playerName = medicStatusMatcher.group("playerName");

            if (playerName.equals(player.getGameProfile().name())) {
                storage.setMedicStatus(parseInt(medicStatusMatcher.group("status")));
            }

            return true;
        }

        Matcher laborTransportStartedMatcher = LABOR_TRANSPORT_STARTED_PATTERN.matcher(message);
        if (laborTransportStartedMatcher.find()) {
            Duration duration = ofMinutes(5).plusSeconds(56); // please don't ask why it is like this
            new Countdown("Labor Transport", duration, () -> {});
            return true;
        }

        Matcher storageIngredientShareMatcher = STORAGE_INGREDIENT_SHARE_PATTERN.matcher(message);
        if (storageIngredientShareMatcher.find()) {
            String playerName = storageIngredientShareMatcher.group("playerName");

            int ingredient1;
            int ingredient2;
            int ingredient3;

            try {
                ingredient1 = parseInt(storageIngredientShareMatcher.group("ingredient1"));
                ingredient2 = parseInt(storageIngredientShareMatcher.group("ingredient2"));
                ingredient3 = parseInt(storageIngredientShareMatcher.group("ingredient3"));
            } catch (NumberFormatException e) {
                LOGGER.warn("Could not parse ingredient amounts from message: {}", message);
                return true;
            }

            player.sendSystemMessage(empty());

            MutableComponent storageText = empty()
                    .append(literal("[").withStyle(DARK_GRAY))
                    .append(STORAGE_TEXT)
                    .append(literal("] ").withStyle(DARK_GRAY))
                    .append(literal("Aktueller Bestand (geteilt von " + playerName + ")").withStyle(GRAY))
                    .append(literal(":").withStyle(DARK_GRAY));

            player.sendSystemMessage(storageText);

            player.sendSystemMessage(getIngredientText("Wirkstoff", ingredient1));
            player.sendSystemMessage(getIngredientText("Trägerstoff", ingredient2));
            player.sendSystemMessage(getIngredientText("Zusatzstoff", ingredient3));

            player.sendSystemMessage(empty());
            return false;
        }

        Matcher storageIngredientAcceptMatcher = STORAGE_INGREDIENT_ACCEPT_PATTERN.matcher(message);
        if (storageIngredientAcceptMatcher.find()) {
            String playerName = storageIngredientAcceptMatcher.group("playerName");
            String ingredient = storageIngredientAcceptMatcher.group("ingredient");
            int amountBefore = parseInt(storageIngredientAcceptMatcher.group("amountBefore"));
            int amountAfter = parseInt(storageIngredientAcceptMatcher.group("amountAfter"));

            player.sendSystemMessage(empty()
                    .append(literal("[").withStyle(DARK_GRAY))
                    .append(STORAGE_TEXT)
                    .append(literal("] ").withStyle(DARK_GRAY))
                    .append(literal(playerName).withStyle(RED))
                    .append(literal(" übernimmt ").withStyle(GRAY))
                    .append(literal(ingredient).withStyle(RED))
                    .append(literal(":").withStyle(DARK_GRAY)));

            player.sendSystemMessage(empty()
                    .append(literal("  > ").withStyle(DARK_GRAY))
                    .append(literal("Geschätzte Menge nach Transport").withStyle(GRAY))
                    .append(literal(": ").withStyle(DARK_GRAY))
                    .append(literal(amountBefore + " → ").withStyle(GRAY))
                    .append(literal(valueOf(amountAfter)).withStyle(getColor(amountAfter), BOLD)));
            return false;
        }

        return true;
    }

    private MutableComponent getIngredientText(String ingredient, int amount) {
        return empty()
                .append(literal("  > ").withStyle(DARK_GRAY))
                .append(literal("[").withStyle(DARK_GRAY))
                .append(literal("Ich übernehme!").withStyle(style -> style
                        .withColor(DARK_AQUA)
                        .withHoverEvent(new HoverEvent.ShowText(literal("Klicke um bescheid zu sagen, dass Du den Transport übernimmst")))
                        .withClickEvent(new ClickEvent.RunCommand("/f Ich nehme " + ingredient + "! (geschätzt: " + amount + " → " + (amount + 20) + ")"))))
                .append(literal("] ").withStyle(DARK_GRAY))
                .append(literal(ingredient).withStyle(GRAY))
                .append(literal(": ").withStyle(DARK_GRAY))
                .append(literal(valueOf(amount)).withStyle(getColor(amount), BOLD));
    }

    private ChatFormatting getColor(int amount) {
        if (amount >= 80) {
            return DARK_GREEN;
        } else if (amount >= 60) {
            return GREEN;
        } else if (amount >= 40) {
            return YELLOW;
        } else if (amount >= 20) {
            return GOLD;
        } else {
            return RED;
        }
    }
}
