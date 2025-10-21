package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
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
                                    String playerName = player.getGameProfile().getName();
                                    Faction faction = storage.getFaction(playerName);
                                    // rank 4 or higher in own faction
                                    return commandService.isSuperUser() || faction.getMembers().stream()
                                            .filter(factionMember -> factionMember.playerName().equals(playerName))
                                            .findFirst()
                                            .map(factionMember -> factionMember.rank() >= 4)
                                            .orElse(false);
                                })
                                .suggests((context, builder) -> {
                                    String playerName = player.getGameProfile().getName();
                                    Faction faction = storage.getFaction(playerName);

                                    return faction != NULL ? suggestMatching(faction.getMembers().stream()
                                            .map(FactionMember::playerName), builder) : empty();
                                })
                                .then(literal("modify")
                                        .then(argument("amount", integer())
                                                .executes(context -> {
                                                    String playerName = player.getGameProfile().getName();
                                                    Faction faction = storage.getFaction(playerName);

                                                    String targetName = getString(context, "player");
                                                    Faction targetFaction = storage.getFaction(targetName);

                                                    int amount = context.getArgument("amount", Integer.class);

                                                    // check faction
                                                    if (faction != targetFaction) {
                                                        messageService.sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                                        return 1;
                                                    }

                                                    api.postMinusPointsModify(playerName, amount);

                                                    return 1;
                                                })))
                                .executes(context -> {
                                    String playerName = player.getGameProfile().getName();
                                    Faction faction = storage.getFaction(playerName);

                                    String targetName = getString(context, "player");
                                    Faction targetFaction = storage.getFaction(targetName);

                                    // check faction
                                    if (faction != targetFaction) {
                                        messageService.sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
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
        api.getMinusPoints(amount -> {
            player.sendMessage(Text.empty(), false);
            messageService.sendModMessage(Text.empty()
                    .append(of("Minuspunkte").copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(valueOf(amount)).copy().formatted(WHITE)), false);
            player.sendMessage(Text.empty(), false);
        });
    }

    private void fetchAndShowMinusPointsFor(String playerName) {
        api.getMinusPointsPlayer(playerName, amount -> {
            player.sendMessage(Text.empty(), false);
            messageService.sendModMessage(Text.empty()
                    .append(of("Minuspunkte für " + playerName).copy().formatted(GRAY))
                    .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(valueOf(amount)).copy().formatted(WHITE)), false);
            player.sendMessage(Text.empty(), false);
        });
    }
}
