package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.api.schema.Activity;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.storage.Storage;
import de.rettichlp.pkutils.common.storage.schema.Reinforcement;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.RED;

@PKUtilsListener
public class FactionListener extends PKUtilsBase implements IMessageReceiveListener, IMessageSendListener, IMoveListener {

    private static final Pattern REINFORCEMENT_PATTERN = compile("^(?:(?<type>.+)! )?(?<senderRank>.+) (?<senderPlayerName>.+) benötigt Unterstützung in der Nähe von (?<naviPoint>.+) \\((?<distance>\\d+)m\\)!$");
    private static final Pattern REINFORCEMENT_BUTTON_PATTERN = compile("^ §7» §cRoute anzeigen §7\\| §cUnterwegs$");
    private static final Pattern REINFORCMENT_ON_THE_WAY_PATTERN = compile("^(?<senderRank>.+) (?<senderPlayerName>.+) kommt zum Verstärkungsruf von (?<target>.+)! \\((?<distance>\\d+) Meter entfernt\\)$");

    private static final ReinforcementConsumer<String, String, String, String> REINFORCEMENT = (type, sender, naviPoint, distance) -> empty()
            .append(of(type).copy().formatted(RED, BOLD)).append(" ")
            .append(of(sender).copy().formatted(AQUA)).append(" ")
            .append(of("-").copy().formatted(GRAY)).append(" ")
            .append(of(naviPoint).copy().formatted(AQUA)).append(" ")
            .append(of("-").copy().formatted(GRAY)).append(" ")
            .append(of(distance + "m").copy().formatted(DARK_AQUA));

    private static final ReinforcementOnTheWayConsumer<String, String, String> REINFORCEMENT_ON_THE_WAY = (sender, target, distance) -> empty()
            .append(of("➥").copy().formatted(GRAY)).append(" ")
            .append(of(sender).copy().formatted(AQUA)).append(" ")
            .append(of("➡").copy().formatted(GRAY)).append(" ")
            .append(of(target).copy().formatted(DARK_AQUA)).append(" ")
            .append(of("- (").copy().formatted(GRAY))
            .append(of(distance + "m").copy().formatted(DARK_AQUA))
            .append(of(")").copy().formatted(GRAY));

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher reinforcementMatcher = REINFORCEMENT_PATTERN.matcher(message);
        if (reinforcementMatcher.find()) {
            String type = ofNullable(reinforcementMatcher.group("type")).orElse("Reinforcement");
            String senderRank = reinforcementMatcher.group("senderRank");
            String senderPlayerName = reinforcementMatcher.group("senderPlayerName");
            String naviPoint = reinforcementMatcher.group("naviPoint");
            String distance = reinforcementMatcher.group("distance");

            Text reinforcementText = REINFORCEMENT.create(type, senderRank + " " + senderPlayerName, naviPoint, distance);
            player.sendMessage(empty(), false);
            player.sendMessage(reinforcementText, false);

            Reinforcement reinforcement = new Reinforcement(type, senderPlayerName, naviPoint, distance);
            storage.trackReinforcement(reinforcement);

            return false;
        }

        Matcher reinforcementButtonMatcher = REINFORCEMENT_BUTTON_PATTERN.matcher(message);
        if (reinforcementButtonMatcher.find()) {
            // send empty line after buttons
            MinecraftClient.getInstance().execute(() -> player.sendMessage(empty(), false));

            List<ClickEvent> clickEvents = text.getSiblings().stream()
                    .map(Text::getStyle)
                    .map(Style::getClickEvent)
                    .filter(Objects::nonNull)
                    .filter(clickEvent -> clickEvent.getAction() == RUN_COMMAND)
                    .toList();

            // origin reinforcement sender retrieving
            String senderName = clickEvents.stream()
                    .map(ClickEvent::getValue)
                    .filter(commandString -> commandString.startsWith("/reinf onway "))
                    .map(commandString -> commandString.replace("/reinf onway ", ""))
                    .toList().getFirst();

            // block position retrieving
            BlockPos blockPos = clickEvents.stream()
                    .map(ClickEvent::getValue)
                    .filter(commandString -> commandString.startsWith("/navi "))
                    .map(commandString -> commandString.replace("/navi ", ""))
                    .map(naviArguments -> {
                        String[] split = naviArguments.split(" ");
                        int x = parseInt(split[0]);
                        int y = parseInt(split[1]);
                        int z = parseInt(split[2]);
                        return new BlockPos(x, y, z);
                    })
                    .toList().getFirst();

            Optional<Reinforcement> optionalReinforcement = storage.getReinforcements().stream()
                    .filter(reinforcement -> reinforcement.getSenderPlayerName().equals(senderName))
                    .max(comparing(Reinforcement::getCreatedAt));

            optionalReinforcement.ifPresent(reinforcement -> reinforcement.setBlockPos(blockPos));

            return true;
        }

        Matcher reinforcementOnTheWayMatcher = REINFORCMENT_ON_THE_WAY_PATTERN.matcher(message);
        if (reinforcementOnTheWayMatcher.find()) {
            String senderRank = reinforcementOnTheWayMatcher.group("senderRank");
            String senderPlayerName = reinforcementOnTheWayMatcher.group("senderPlayerName");
            String target = reinforcementOnTheWayMatcher.group("target");
            String distance = reinforcementOnTheWayMatcher.group("distance");

            Text reinforcementAnswer = REINFORCEMENT_ON_THE_WAY.create(senderRank + " " + senderPlayerName, target, distance);
            player.sendMessage(reinforcementAnswer, false);

            storage.getReinforcements().stream()
                    .filter(reinforcement -> reinforcement.getSenderPlayerName().equals(target)) // get reinforcements of target
                    .max(comparing(Reinforcement::getCreatedAt)) // get the latest one
                    .ifPresent(reinforcement -> reinforcement.getAcceptedPlayerNames().add(senderPlayerName));

            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageSend(String message) {
        Storage.ToggledChat toggledChat = storage.getToggledChat();
        if (toggledChat != NONE) {
            networkHandler.sendChatCommand(toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }

    @Override
    public void onMove(BlockPos blockPos) {
        String playerName = player.getGameProfile().getName();

        // get the nearest reinforcement within 60 blocks that is not from yourself and was accepted
        Optional<Reinforcement> optionalReinforcement = storage.getReinforcements().stream()
                .filter(reinforcement -> nonNull(reinforcement.getBlockPos())) // check if the block position was set
                .filter(reinforcement -> reinforcement.getBlockPos().isWithinDistance(player.getBlockPos(), 60))
                .filter(reinforcement -> !reinforcement.getSenderPlayerName().equals(playerName))
                .filter(reinforcement -> reinforcement.getAcceptedPlayerNames().contains(playerName))
                .max(comparing(Reinforcement::getCreatedAt));

        optionalReinforcement.ifPresent(reinforcement -> {
            if (reinforcement.isAddedAsActivity()) {
                LOGGER.info("Reinforcement already added as activity");
                return;
            }

            reinforcement.setAddedAsActivity(true);
            api.trackActivity(Activity.Type.REINFORCEMENT);
            LOGGER.info("Reinforcement reached, tracked activity");
        });
    }

    @FunctionalInterface
    public interface ReinforcementConsumer<Type, Sender, NaviPoint, Distance> {

        Text create(String type, String sender, String naviPoint, String distance);
    }

    @FunctionalInterface
    public interface ReinforcementOnTheWayConsumer<Sender, Target, Distance> {

        Text create(String sender, String target, String distance);
    }
}
