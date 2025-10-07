package de.rettichlp.pkutils.command.faction;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.lang.String.valueOf;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "eigenbedarf")
public class PersonalUseCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("give")
                        .then(argument("player", word())
                                .suggests((context, builder) -> {
                                    List<String> list = networkHandler.getPlayerList().stream()
                                            .map(PlayerListEntry::getProfile)
                                            .map(GameProfile::getName)
                                            .toList();
                                    return suggestMatching(list, builder);
                                })
                                .executes(context -> {
                                    String targetPlayer = getString(context, "player");
                                    sendCommands(createCommands("selldrug " + targetPlayer + " %name% %amount% %purity% 0"));
                                    return 1;
                                })))
                .executes(context -> {
                    sendCommands(createCommands("dbank get %name% %amount% %purity%"));
                    return 1;
                });
    }

    private @NotNull List<String> createCommands(String commandTemplate) {
        List<String> commandStrings = configService.load().getOptions().personalUse().stream()
                .filter(personalUseEntry -> personalUseEntry.getAmount() > 0)
                .map(personalUseEntry -> commandTemplate
                        .replace("%name%", personalUseEntry.getInventoryItem().getDisplayName())
                        .replace("%amount%", valueOf(personalUseEntry.getAmount()))
                        .replace("%purity%", valueOf(personalUseEntry.getPurity().ordinal())))
                .toList();

        if (commandStrings.isEmpty()) {
            sendModMessage("Du hast keinen Eigenbedarf gesetzt.", false);
        }

        return commandStrings;
    }
}
