package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.Storage;
import de.rettichlp.pkutils.common.models.ActivityEntry;
import de.rettichlp.pkutils.common.models.BlackMarket;
import de.rettichlp.pkutils.common.models.Reinforcement;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.models.EquipEntry.Type.fromDisplayName;
import static de.rettichlp.pkutils.common.models.Faction.FBI;
import static de.rettichlp.pkutils.common.models.Faction.RETTUNGSDIENST;
import static de.rettichlp.pkutils.common.models.config.Options.ReinforcementType.UNICACITYADDON;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
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
    private static final Pattern EQUIP_PATTERN = compile("^\\[Equip] Du hast dich mit (?<type>.+) equipt!$");

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

            Reinforcement reinforcement = new Reinforcement(type, senderPlayerName, naviPoint, distance);
            LOGGER.info("Found new reinforcement: {}", reinforcement);
            storage.trackReinforcement(reinforcement);

            boolean modernReinforcementStyle = configService.load().getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                Text reinforcementText = REINFORCEMENT.create(type, senderRank + " " + senderPlayerName, naviPoint, distance);
                player.sendMessage(empty(), false);
                player.sendMessage(reinforcementText, false);
            }

            return !modernReinforcementStyle;
        }

        Matcher reinforcementButtonMatcher = REINFORCEMENT_BUTTON_PATTERN.matcher(message);
        if (reinforcementButtonMatcher.find()) {
            boolean modernReinforcementStyle = configService.load().getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                // send empty line after buttons
                MinecraftClient.getInstance().execute(() -> player.sendMessage(empty(), false));
            }

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

            LOGGER.info("Found reinforcement buttons: {} | {}", senderName, blockPos.toShortString());

            storage.getReinforcements().stream()
                    .filter(reinforcement -> reinforcement.getSenderPlayerName().equals(senderName))
                    .max(comparing(Reinforcement::getCreatedAt))
                    .ifPresent(reinforcement -> {
                        reinforcement.setBlockPos(blockPos);
                        LOGGER.info("Updated reinforcement: {}", reinforcement);
                    });

            return true;
        }

        Matcher reinforcementOnTheWayMatcher = REINFORCMENT_ON_THE_WAY_PATTERN.matcher(message);
        if (reinforcementOnTheWayMatcher.find()) {
            String senderRank = reinforcementOnTheWayMatcher.group("senderRank");
            String senderPlayerName = reinforcementOnTheWayMatcher.group("senderPlayerName");
            String target = reinforcementOnTheWayMatcher.group("target");
            String distance = reinforcementOnTheWayMatcher.group("distance");

            // mark all reinforcements of the sender (and not self) within the last 30 seconds as on-the-way
            String playerName = player.getGameProfile().getName();
            storage.getReinforcements().stream()
                    .filter(reinforcement -> playerName.equals(senderPlayerName)) // the client is on the way
                    .filter(reinforcement -> !playerName.equals(target)) // don't accept your own reinforcement
                    .filter(reinforcement -> reinforcement.getSenderPlayerName().equals(target)) // find all reinforcements of the target
                    .filter(reinforcement -> !reinforcement.getAcceptedPlayerNames().contains(senderPlayerName)) // not already accepted by the player
                    .filter(reinforcement -> reinforcement.getCreatedAt().isAfter(now().minusSeconds(30))) // only recent ones
                    .filter(reinforcement -> switch (reinforcement.getType()) { // reinforcement types that should be accepted
                        case "Medic benötigt" ->
                                storage.getFaction(senderPlayerName) == RETTUNGSDIENST; // only medics accept medic calls
                        case "Drogenabnahme" -> storage.getFaction(senderPlayerName) == FBI; // only FBI accept drug bust calls
                        default -> true; // all others accept all calls
                    })
                    .forEach(reinforcement -> {
                        reinforcement.getAcceptedPlayerNames().add(senderPlayerName);
                        LOGGER.info("Reinforcement accepted: {}", reinforcement);
                    });

            boolean modernReinforcementStyle = configService.load().getOptions().reinforcementType() == UNICACITYADDON;
            if (modernReinforcementStyle) {
                Text reinforcementAnswer = REINFORCEMENT_ON_THE_WAY.create(senderRank + " " + senderPlayerName, target, distance);
                player.sendMessage(reinforcementAnswer, false);
            }

            return !modernReinforcementStyle;
        }

        Matcher equipMatcher = EQUIP_PATTERN.matcher(message);
        if (equipMatcher.find()) {
            String type = equipMatcher.group("type");
            fromDisplayName(type).ifPresent(api::trackEquip);
            return true;
        }

        return true;
    }

    @Override
    public boolean onMessageSend(String message) {
        Storage.ToggledChat toggledChat = storage.getToggledChat();
        if (toggledChat != NONE) {
            sendCommand(toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }

    @Override
    public void onMove(BlockPos blockPos) {
        String playerName = player.getGameProfile().getName();

        // for all reinforcements within 60 blocks that were not from yourself and were accepted
        storage.getReinforcements().stream()
                .filter(reinforcement -> nonNull(reinforcement.getBlockPos())) // check if the block position was set
                .filter(reinforcement -> reinforcement.getBlockPos().isWithinDistance(player.getBlockPos(), 60))
                .filter(reinforcement -> reinforcement.getAcceptedPlayerNames().contains(playerName))
                .filter(reinforcement -> !reinforcement.isAddedAsActivity())
                .forEach(reinforcement -> {
                    reinforcement.setAddedAsActivity(true);
                    api.trackActivity(ActivityEntry.Type.REINFORCEMENT);
                    LOGGER.info("Reinforcement reached, tracked activity");
                });

        // mark the black market spot as visited if within 60 blocks
        stream(BlackMarket.Type.values())
                .filter(type -> type.getBlockPos().isWithinDistance(blockPos, 60))
                .forEach(type -> {
                    // remove old type association if exists
                    storage.getBlackMarkets().removeIf(blackMarket -> blackMarket.getType() == type);

                    // check if black market was found there
                    Box box = player.getBoundingBox().expand(60);
                    Predicate<VillagerEntity> isBlackMarket = villagerEntity -> ofNullable(villagerEntity.getCustomName())
                            .map(text -> text.getString().contains("Schwarzmarkt"))
                            .orElse(false);

                    assert MinecraftClient.getInstance().world != null; // cannot be null at this point
                    boolean found = !MinecraftClient.getInstance().world.getEntitiesByClass(VillagerEntity.class, box, isBlackMarket).isEmpty();

                    // add new black market entry
                    BlackMarket blackMarket = new BlackMarket(type, now(), found);
                    storage.getBlackMarkets().add(blackMarket);
                    LOGGER.info("Marked black market spot as visited: {}", type);
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
