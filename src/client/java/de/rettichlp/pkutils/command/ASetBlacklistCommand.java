package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.BlacklistReason;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "asetbl")
public class ASetBlacklistCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        Faction faction = storage.getFaction(player.getName().getString());
        List<BlacklistReason> blacklistReasons = storage.getBlacklistReasons().getOrDefault(faction, new ArrayList<>());
        List<String> blacklistReasonStrings = blacklistReasons.stream()
                .map(BlacklistReason::getReason)
                .map(reason -> reason.replace(" ", "_"))
                .toList();

        return node
                .then(argument("reason", word())
                        .suggests((context, builder) -> suggestMatching(blacklistReasonStrings, builder))
                        .then(argument("players", greedyString())
                                .suggests((context, builder) -> {
                                    List<String> list = networkHandler.getPlayerList().stream()
                                            .map(PlayerListEntry::getProfile)
                                            .map(GameProfile::getName)
                                            .toList();
                                    return suggestMatching(list, builder);
                                })
                                .executes(context -> {
                                    String reasonString = getString(context, "reason").replace("_", " ");
                                    String playersString = getString(context, "players");
                                    String[] playerArray = playersString.split(" ");

                                    if (playerArray.length == 0) {
                                        sendModMessage("Keine Spieler angegeben.", false);
                                        return 1;
                                    }

                                    Optional<BlacklistReason> optionalBlacklistReason = blacklistReasons.stream()
                                            .filter(blacklistReason -> blacklistReason.getReason().equalsIgnoreCase(reasonString))
                                            .findFirst();

                                    if (optionalBlacklistReason.isEmpty()) {
                                        sendModMessage("Der Grund \"" + reasonString + "\" ist ungültig.", false);
                                        return 1;
                                    }

                                    BlacklistReason blacklistReason = optionalBlacklistReason.get();

                                    List<String> blacklistCommands = stream(playerArray)
                                            .map(playerName -> "blacklist add " + playerName + " " + blacklistReason.getPrice() + " " + blacklistReason.getKills() + " " + blacklistReason.getReason())
                                            .toList();

                                    new Timer().scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
                                            networkHandler.sendChatCommand(blacklistCommands.removeFirst());
                                        }
                                    }, 0, 1000);

                                    return 1;
                                })));
    }
}
