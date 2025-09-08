package de.rettichlp.pkutils.command.activity;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.api.schema.Activity;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static java.lang.Integer.MIN_VALUE;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.ZonedDateTime.now;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

@PKUtilsCommand(label = "activity")
public class ActivityCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("weeksAgo", integer(MIN_VALUE, 0))
                        .suggests((context, builder) -> suggestMatching(List.of("0", "-1", "-2", "-3", "-4", "-5"), builder))
                        .executes(context -> {
                            int weeksAgo = context.getArgument("weeksAgo", Integer.class);
                            fetchAndShowActivitiesFor(weeksAgo);
                            return 1;
                        }))
                .then(literal("player")
                        .then(argument("player", word())
                                .requires(fabricClientCommandSource -> {
                                    String playerName = player.getName().getString();
                                    Faction faction = storage.getFaction(playerName);
                                    // rank 4 or higher in own faction
                                    return isSuperUser() || storage.getFactionMembers(faction).stream()
                                            .filter(factionMember -> factionMember.playerName().equals(playerName))
                                            .findFirst()
                                            .map(factionMember -> factionMember.rank() >= 4)
                                            .orElse(false);
                                })
                                .suggests((context, builder) -> {
                                    String playerName = player.getName().getString();
                                    Faction faction = storage.getFaction(playerName);

                                    return faction != NULL ? suggestMatching(faction.getMembers().stream()
                                            .map(FactionMember::playerName), builder) : empty();
                                })
                                .then(argument("weeksAgo", integer(MIN_VALUE, 0))
                                        .suggests((context, builder) -> suggestMatching(List.of("0", "-1", "-2", "-3", "-4", "-5"), builder))
                                        .executes(context -> {
                                            String playerName = player.getName().getString();
                                            Faction faction = storage.getFaction(playerName);

                                            String targetName = getString(context, "player");
                                            Faction targetFaction = storage.getFaction(targetName);

                                            int weeksAgo = context.getArgument("weeksAgo", Integer.class);

                                            // check faction
                                            if (faction != targetFaction) {
                                                sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                                return 1;
                                            }

                                            fetchAndShowActivitiesFor(targetName, weeksAgo);

                                            return 1;
                                        }))
                                .executes(context -> {
                                    String playerName = player.getName().getString();
                                    Faction faction = storage.getFaction(playerName);

                                    String targetName = getString(context, "player");
                                    Faction targetFaction = storage.getFaction(targetName);

                                    // check faction
                                    if (faction != targetFaction) {
                                        sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                        return 1;
                                    }

                                    fetchAndShowActivitiesFor(targetName, 0);

                                    return 1;
                                })))
                .executes(context -> {
                    fetchAndShowActivitiesFor(0);
                    return 1;
                });
    }

    private void fetchAndShowActivitiesFor(int relativeWeekIndex) {
        Range range = getRange(relativeWeekIndex);
        CompletableFuture<List<Activity>> activitiesFuture = api.getActivities(range.fromZonedDateTime().toInstant(), range.toZonedDateTime.toInstant());

        activitiesFuture.thenAccept(activities -> {
            // summarize by type
            Map<Activity.Type, Long> activityAmountPerType = activities.stream()
                    .collect(groupingBy(Activity::type, counting()));

            player.sendMessage(Text.empty(), false);
            sendModMessage("Aktivitäten:", false);
            activityAmountPerType.forEach((type, amount) -> sendModMessage(Text.empty()
                    .append(of(type.getDisplayMessage()).copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(amount + "x").copy().formatted(WHITE)), false));
            player.sendMessage(Text.empty(), false);
        });
    }

    private void fetchAndShowActivitiesFor(String playerName, int relativeWeekIndex) {
        Range range = getRange(relativeWeekIndex);
        CompletableFuture<List<Activity>> activitiesFuture = api.getActivitiesForPlayer(playerName, range.fromZonedDateTime().toInstant(), range.toZonedDateTime.toInstant());

        activitiesFuture.thenAccept(activities -> {
            // summarize by type
            Map<Activity.Type, Long> activityAmountPerType = activities.stream()
                    .collect(groupingBy(Activity::type, counting()));

            player.sendMessage(Text.empty(), false);
            sendModMessage("Aktivitäten von " + playerName + ":", false);
            activityAmountPerType.forEach((type, amount) -> sendModMessage(Text.empty()
                    .append(of(type.getDisplayMessage()).copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(amount + "x").copy().formatted(WHITE)), false));
            player.sendMessage(Text.empty(), false);
        });
    }

    private @NotNull Range getRange(int relativeWeekIndex) {
        ZonedDateTime now = now(ZoneId.of("Europe/Berlin"));

        // friday 20 o'clock
        ZonedDateTime friday20 = now
                .with(nextOrSame(FRIDAY))
                .withHour(20)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        ZonedDateTime fromZonedDateTime;
        ZonedDateTime toZonedDateTime;

        if (now.isBefore(friday20)) {
            fromZonedDateTime = friday20.minusWeeks(1).plusWeeks(relativeWeekIndex);
            toZonedDateTime = friday20.plusWeeks(relativeWeekIndex);
        } else {
            fromZonedDateTime = friday20.plusWeeks(relativeWeekIndex);
            toZonedDateTime = friday20.plusWeeks(1).plusWeeks(relativeWeekIndex);
        }

        return new Range(fromZonedDateTime, toZonedDateTime);
    }

    private record Range(ZonedDateTime fromZonedDateTime, ZonedDateTime toZonedDateTime) {}
}
