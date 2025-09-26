package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.BlacklistReason;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "asetbl")
public class ASetBlacklistCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("reason", string())
                        .suggests((context, builder) -> {
                            Faction faction = storage.getFaction(player.getName().getString());
                            List<String> blacklistReasonStrings = storage.getBlacklistReasons().getOrDefault(faction, new ArrayList<>()).stream()
                                    .map(BlacklistReason::getReason)
                                    .map(reasonString -> reasonString.replace(" ", "_"))
                                    .map(reasonString -> "\"" + reasonString + "\"")
                                    .toList();
                            return suggestMatching(blacklistReasonStrings, builder);
                        })
                        .then(argument("player1", word())
                                .suggests((context, builder) -> suggestPlayerNames(builder))
                                .then(argument("player2", word())
                                        .suggests((context, builder) -> suggestPlayerNames(builder))
                                        .then(argument("player3", word())
                                                .suggests((context, builder) -> suggestPlayerNames(builder))
                                                .then(argument("player4", word())
                                                        .suggests((context, builder) -> suggestPlayerNames(builder))
                                                        .then(argument("player5", word())
                                                                .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                .then(argument("player6", word())
                                                                        .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                        .then(argument("player7", word())
                                                                                .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                                .then(argument("player8", word())
                                                                                        .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                                        .then(argument("player9", word())
                                                                                                .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                                                .then(argument("player10", word())
                                                                                                        .suggests((context, builder) -> suggestPlayerNames(builder))
                                                                                                        .executes(this::execute))
                                                                                                .executes(this::execute))
                                                                                        .executes(this::execute))
                                                                                .executes(this::execute))
                                                                        .executes(this::execute))
                                                                .executes(this::execute))
                                                        .executes(this::execute))
                                                .executes(this::execute))
                                        .executes(this::execute))
                                .executes(this::execute)));
    }

    private int execute(@NotNull CommandContext<FabricClientCommandSource> context) {
        List<String> argumentNames = context.getNodes().stream()
                .map(ParsedCommandNode::getNode)
                .map(CommandNode::getName)
                .toList();

        String reasonString = getString(context, "reason").replace("_", " ").replace("\"", "");

        Set<String> playerNames = range(1, 11)
                .mapToObj(operand -> "player" + operand)
                .filter(argumentNames::contains)
                .map(argumentName -> getString(context, argumentName))
                .collect(toSet());

        if (playerNames.isEmpty()) {
            sendModMessage("Keine Spieler angegeben.", false);
            return 1;
        }

        Faction faction = storage.getFaction(player.getName().getString());
        Optional<BlacklistReason> optionalBlacklistReason = storage.getBlacklistReasons().getOrDefault(faction, new ArrayList<>()).stream()
                .filter(blacklistReason -> blacklistReason.getReason().equalsIgnoreCase(reasonString))
                .findFirst();

        if (optionalBlacklistReason.isEmpty()) {
            sendModMessage("Der Grund \"" + reasonString + "\" ist PKUtils unbekannt.", false);
            return 1;
        }

        BlacklistReason blacklistReason = optionalBlacklistReason.get();

        List<String> blacklistCommands = new ArrayList<>();

        for (String playerName : playerNames) {
            // get current blacklist entry for player
            Optional<BlacklistEntry> optionalBlacklistEntry = storage.getBlacklistEntries().stream()
                    .filter(blacklistEntry -> blacklistEntry.getPlayerName().equals(playerName))
                    .findFirst();

            // check if the player is already blacklisted for the same reason
            if (optionalBlacklistEntry.isPresent()) {
                BlacklistEntry blacklistEntry = optionalBlacklistEntry.get();
                if (blacklistEntry.getReason().contains(reasonString)) {
                    sendModMessage(playerName + " ist bereits für den Grund \"" + reasonString + "\" auf der Blacklist.", false);
                    continue;
                }
            }

            // create and schedule command
            blacklistCommands.add("blacklist add " + playerName + " " + blacklistReason.getPrice() + " " + blacklistReason.getKills() + " " + blacklistReason.getReason());
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (blacklistCommands.isEmpty()) {
                    this.cancel();
                    return;
                }

                sendCommand(blacklistCommands.removeFirst());
            }
        }, 0, 1000);

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlayerNames(SuggestionsBuilder builder) {
        List<String> list = networkHandler.getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getName)
                .toList();
        return suggestMatching(list, builder);
    }
}
