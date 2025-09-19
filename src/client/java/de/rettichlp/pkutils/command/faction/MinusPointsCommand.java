package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.lang.String.valueOf;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

@PKUtilsCommand(label = "minuspoints")
public class MinusPointsCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
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
                                .then(literal("modify")
                                        .then(argument("amount", integer())
                                                .executes(context -> {
                                                    String playerName = player.getName().getString();
                                                    Faction faction = storage.getFaction(playerName);

                                                    String targetName = getString(context, "player");
                                                    Faction targetFaction = storage.getFaction(targetName);

                                                    int amount = context.getArgument("amount", Integer.class);

                                                    // check faction
                                                    if (faction != targetFaction) {
                                                        sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                                        return 1;
                                                    }

                                                    api.modifyMinusPoints(targetName, amount).thenAccept(integer -> hudService.sendInfoNotification("Minuspunkte für " + targetName + " auf " + integer + " gesetzt."));

                                                    return 1;
                                                })))
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

                                    fetchAndShowMinusPointsFor(targetName);

                                    return 1;
                                })))
                .executes(context -> {
                    fetchAndShowMinusPointsFor();
                    return 1;
                });
    }

    private void fetchAndShowMinusPointsFor() {
        CompletableFuture<Integer> minusPoints = api.getMinusPoints();

        minusPoints.thenAccept(amount -> {
            player.sendMessage(Text.empty(), false);
            sendModMessage(Text.empty()
                    .append(of("Minuspunkte").copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(valueOf(amount)).copy().formatted(WHITE)), false);
            player.sendMessage(Text.empty(), false);
        });
    }

    private void fetchAndShowMinusPointsFor(String playerName) {
        CompletableFuture<Integer> minusPoints = api.getMinusPointsForPlayer(playerName);

        minusPoints.thenAccept(amount -> {
            player.sendMessage(Text.empty(), false);
            sendModMessage(Text.empty()
                    .append(of("Minuspunkte für " + playerName).copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(valueOf(amount)).copy().formatted(WHITE)), false);
            player.sendMessage(Text.empty(), false);
        });
    }
}
