package de.rettichlp.pkutils.command.faction;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.networkHandler;
import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "eigenbedarf")
public class PersonalUseCommand extends CommandBase implements IMessageReceiveListener {

    private static final Pattern DEAL_ACCEPTED = compile("^\\[Deal] (?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat den Deal angenommen\\.$");
    private static final Pattern DEAL_DECLINED = compile("^(?:\\[PK])?(?<playerName>[a-zA-Z0-9_]+) hat das Angebot abgelehnt\\.$");

    private List<String> commands = new ArrayList<>();

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
                                    this.commands = new ArrayList<>(createCommands("selldrug " + targetPlayer + " %name% %amount% %purity% 0"));
                                    removeAndExecuteFirst();
                                    return 1;
                                })))
                .executes(context -> {
                    commandService.sendCommands(createCommands("dbank get %name% %amount% %purity%"));
                    return 1;
                });
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        if (DEAL_ACCEPTED.matcher(message).find() || DEAL_DECLINED.matcher(message).find()) {
            removeAndExecuteFirst();
        }

        return true;
    }

    private @NotNull List<String> createCommands(String commandTemplate) {
        List<String> commandStrings = configuration.getOptions().personalUse().stream()
                .filter(personalUseEntry -> personalUseEntry.getAmount() > 0)
                .map(personalUseEntry -> commandTemplate
                        .replace("%name%", personalUseEntry.getInventoryItem().getDisplayName())
                        .replace("%amount%", valueOf(personalUseEntry.getAmount()))
                        .replace("%purity%", valueOf(personalUseEntry.getPurity().ordinal())))
                .toList();

        if (commandStrings.isEmpty()) {
            messageService.sendModMessage("Du hast keinen Eigenbedarf gesetzt.", false);
        }

        return commandStrings;
    }

    private void removeAndExecuteFirst() {
        if (!this.commands.isEmpty()) {
            String firstCommandString = this.commands.removeFirst();
            commandService.sendCommand(firstCommandString);
        }
    }
}
