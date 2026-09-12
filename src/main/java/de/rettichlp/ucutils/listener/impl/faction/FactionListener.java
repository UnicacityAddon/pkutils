package de.rettichlp.ucutils.listener.impl.faction;

import de.rettichlp.ucutils.common.Storage;
import de.rettichlp.ucutils.common.models.Faction;
import de.rettichlp.ucutils.common.models.FactionMember;
import de.rettichlp.ucutils.common.registry.UCUtilsListener;
import de.rettichlp.ucutils.listener.IMessageReceiveListener;
import de.rettichlp.ucutils.listener.IMessageSendListener;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.ucutils.UCUtils.MOD_NAME;
import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static de.rettichlp.ucutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.ucutils.common.configuration.options.Options.ReinforcementType.UNICACITYADDON;
import static de.rettichlp.ucutils.common.models.Faction.RETTUNGSDIENST;
import static java.awt.Color.MAGENTA;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.network.chat.TextColor.AQUA;
import static net.minecraft.network.chat.TextColor.DARK_AQUA;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.RED;

@UCUtilsListener
public class FactionListener implements IMessageReceiveListener, IMessageSendListener {

    private static final Pattern DUTY_PATTERN = compile("^(Du bist nun als Arzt im Dienst!|Du hast den Dienst wieder angetreten\\.)$");

    private static final Pattern REINFORCEMENT_PATTERN = compile("^(?:(?<type>.+)! )?(?<senderRank>.+) (?:\\[UC])?(?<senderPlayerName>[a-zA-Z0-9_]+) benötigt Unterstützung in der Nähe von (?<naviPoint>.+)! \\((?<distance>\\d+) Meter entfernt\\)$");
    private static final Pattern REINFORCEMENT_BUTTON_PATTERN = compile("^§7» §c§lRoute anzeigen §8\\| §c§lUnterwegs$");
    private static final Pattern REINFORCMENT_ON_THE_WAY_PATTERN = compile("^(?<senderRank>.+) (?:\\[UC])?(?<senderPlayerName>[a-zA-Z0-9_]+) kommt zum Verstärkungsruf von (?:\\[UC])?(?<target>[a-zA-Z0-9_]+)! \\((?<distance>\\d+) Meter entfernt\\)$");

    private static final Pattern FACTION_CHAT_PATTERN = compile("^(?<playerPrefix>[\\p{L} ]+) (?:\\[UC])?(?<senderPlayerName>[a-zA-Z0-9_]+): (?<message>.+)$");

    private static final ReinforcementConsumer<String, String, String, String> REINFORCEMENT = (type, sender, naviPoint, distance) -> empty()
            .append(literal(type).withColor(RED).withStyle(BOLD)).append(SPACE)
            .append(literal(sender).withColor(AQUA)).append(SPACE)
            .append(literal("-").withColor(GRAY)).append(SPACE)
            .append(literal(naviPoint).withColor(AQUA)).append(SPACE)
            .append(literal("-").withColor(GRAY)).append(SPACE)
            .append(literal(distance + "m").withColor(DARK_AQUA));

    private static final ReinforcementOnTheWayConsumer<String, String, String> REINFORCEMENT_ON_THE_WAY = (sender, target, distance) -> empty()
            .append(literal("➥").withColor(GRAY)).append(SPACE)
            .append(literal(sender).withColor(AQUA)).append(SPACE)
            .append(literal("➡").withColor(GRAY)).append(SPACE)
            .append(literal(target).withColor(DARK_AQUA)).append(SPACE)
            .append(literal("- (").withColor(GRAY))
            .append(literal(distance + "m").withColor(DARK_AQUA))
            .append(literal(")").withColor(GRAY));

    private boolean isReinforcementRelevantForFaction = false;

    @Override
    public boolean onMessageReceive(Component text, String message) {
        Matcher dutyMatcher = DUTY_PATTERN.matcher(message);
        if (dutyMatcher.find()) {
            if (configuration.getOptions().miscellaneous().autoSiren()) {
                commandService.sendCommand("sirene");
            }
            return true;
        }

        Matcher reinforcementMatcher = REINFORCEMENT_PATTERN.matcher(message);
        if (reinforcementMatcher.find()) {
            String type = ofNullable(reinforcementMatcher.group("type")).orElse("Reinforcement");
            String senderRank = reinforcementMatcher.group("senderRank");
            String senderPlayerName = reinforcementMatcher.group("senderPlayerName");
            String naviPoint = reinforcementMatcher.group("naviPoint");
            String distance = reinforcementMatcher.group("distance");

            // save reinforcement sender if relevant for faction
            Faction faction = storage.getFaction(player.getPlainTextName());
            boolean isMedicRequest = type.equals("Medic benötigt");
            this.isReinforcementRelevantForFaction = (faction == RETTUNGSDIENST) == isMedicRequest;
            if (this.isReinforcementRelevantForFaction) {
                storage.setLastRelevantReinforcementSenderName(senderPlayerName);
            }

            boolean modernReinforcementStyle = configuration.getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                Component reinforcementText = REINFORCEMENT.create(type, senderRank + " " + senderPlayerName, naviPoint, distance);
                player.sendSystemMessage(empty());
                player.sendSystemMessage(reinforcementText);
            }

            return !modernReinforcementStyle;
        }

        Matcher reinforcementButtonMatcher = REINFORCEMENT_BUTTON_PATTERN.matcher(message);
        if (reinforcementButtonMatcher.find()) {
            if (this.isReinforcementRelevantForFaction) {
                player.sendSystemMessage(text.copy().append(SPACE).append(literal("✨").withStyle(style -> style
                        .withColor(MAGENTA.brighter().getRGB())
                        .withBold(true)
                        .withHoverEvent(new HoverEvent.ShowText(translatable("ucutils.reinforcement_hotkey_available", MOD_NAME))))));
            }

            boolean modernReinforcementStyle = configuration.getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                // send empty line after buttons
                utilService.delayedAction(() -> player.sendSystemMessage(empty()), 50);
            }

            return !this.isReinforcementRelevantForFaction;
        }

        Matcher reinforcementOnTheWayMatcher = REINFORCMENT_ON_THE_WAY_PATTERN.matcher(message);
        if (reinforcementOnTheWayMatcher.find()) {
            String senderRank = reinforcementOnTheWayMatcher.group("senderRank");
            String senderPlayerName = reinforcementOnTheWayMatcher.group("senderPlayerName");
            String target = reinforcementOnTheWayMatcher.group("target");
            String distance = reinforcementOnTheWayMatcher.group("distance");

            boolean modernReinforcementStyle = configuration.getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                Component reinforcementAnswer = REINFORCEMENT_ON_THE_WAY.create(senderRank + " " + senderPlayerName, target, distance);
                player.sendSystemMessage(reinforcementAnswer);
            }

            return !modernReinforcementStyle;
        }

        Matcher factionChatMatcher = FACTION_CHAT_PATTERN.matcher(message);
        if (factionChatMatcher.find()) {
            if (!configuration.getOptions().chatOptions().changeFactionChatColor()) {
                return true;
            }

            int primaryColorValue = configuration.getOptions().chatOptions().factionChatColorPrimary().getRGB();
            int secondaryColorValue = configuration.getOptions().chatOptions().factionChatColorSecondary().getRGB();

            // check if color already matches formatting
            List<Component> siblings = text.getSiblings();
            if (siblings.size() != 3 || messageMatchesColor(siblings, primaryColorValue, secondaryColorValue)) {
                return true;
            }

            String playerPrefix = factionChatMatcher.group("playerPrefix");
            String senderPlayerName = factionChatMatcher.group("senderPlayerName");
            String factionMessage = factionChatMatcher.group("message");

            Optional<FactionMember> optionalFactionMember = storage.getFactionMember(senderPlayerName);
            if (optionalFactionMember.isEmpty()) {
                return true;
            }

            String rankName = optionalFactionMember.get().rankName();
            if (!playerPrefix.equals(rankName)) {
                return true;
            }

            player.sendSystemMessage(empty()
                    .append(literal(playerPrefix).withColor(primaryColorValue))
                    .append(literal(" "))
                    .append(literal(senderPlayerName).withColor(secondaryColorValue))
                    .append(literal(": ").withColor(DARK_GRAY))
                    .append(literal(factionMessage).withColor(secondaryColorValue)));

            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageSend(String message) {
        Storage.ToggledChat toggledChat = storage.getToggledChat();
        if (toggledChat != NONE) {
            commandService.sendCommand(toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }

    private boolean messageMatchesColor(@NonNull List<Component> siblings, int primaryColorValue, int secondaryColorValue) {
        TextColor primaryCurrent = siblings.get(0).getStyle().getColor();
        TextColor secondaryCurrent = siblings.get(2).getStyle().getColor();
        return primaryCurrent == null || secondaryCurrent == null || primaryCurrent.getValue() == primaryColorValue || secondaryCurrent.getValue() == secondaryColorValue;
    }

    @FunctionalInterface
    public interface ReinforcementConsumer<Type, Sender, NaviPoint, Distance> {

        Component create(String type, String sender, String naviPoint, String distance);
    }

    @FunctionalInterface
    public interface ReinforcementOnTheWayConsumer<Sender, Target, Distance> {

        Component create(String sender, String target, String distance);
    }
}
