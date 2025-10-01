package de.rettichlp.pkutils.command.faction;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.InventoryItem;
import de.rettichlp.pkutils.common.models.OwnUseEntry;
import de.rettichlp.pkutils.common.models.config.MainConfig;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.common.models.InventoryItem.fromDisplayName;
import static java.lang.Integer.MAX_VALUE;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "eigenbedarf")
public class OwnUseCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("set")
                        .then(argument("type", string())
                                .suggests((context, builder) -> {
                                    List<String> inventoryItemStrings = Arrays.stream(InventoryItem.values())
                                            .filter(InventoryItem::isDrugBankItem)
                                            .map(InventoryItem::getDisplayName)
                                            .map(inventoryItemString -> "\"" + inventoryItemString + "\"")
                                            .toList();
                                    return suggestMatching(inventoryItemStrings, builder);
                                })
                                .then(argument("purity", integer(0, 3))
                                        .then(argument("amount", integer(1, MAX_VALUE))
                                                .executes(context -> {
                                                    String inventoryItemString = getString(context, "type").replace("\"", "");
                                                    Optional<InventoryItem> optionalInventoryItem = fromDisplayName(inventoryItemString);
                                                    int purity = getInteger(context, "purity");
                                                    int amount = getInteger(context, "amount");

                                                    if (optionalInventoryItem.isEmpty()) {
                                                        sendModMessage(inventoryItemString + " ist keine valide Eingabe.", true);
                                                        return 1;
                                                    }

                                                    InventoryItem inventoryItem = optionalInventoryItem.get();

                                                    configService.edit(mainConfig -> {
                                                        // remove old entry if exists
                                                        mainConfig.getOwnUseEntries().removeIf(ownUseEntry -> ownUseEntry.inventoryItem() == inventoryItem);

                                                        // add new entry
                                                        OwnUseEntry ownUseEntry = new OwnUseEntry(inventoryItem, purity, amount);
                                                        mainConfig.getOwnUseEntries().add(ownUseEntry);

                                                        sendModMessage("Eigenbedarf für " + inventoryItem.getDisplayName() + " auf " + amount + " (Reinheit: " + purity + ") gesetzt.", false);
                                                    });

                                                    return 1;
                                                })))))
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
                    sendCommands(createCommands("dbank get %name% %purity% %amount%"));
                    return 1;
                });
    }

    private @NotNull List<String> createCommands(String commandTemplate) {
        MainConfig mainConfig = configService.load();
        List<OwnUseEntry> ownUseEntries = mainConfig.getOwnUseEntries();

        List<String> commandStrings = new ArrayList<>();

        for (OwnUseEntry ownUseEntry : ownUseEntries) {
            if (ownUseEntry.amount() <= 0) {
                continue;
            }

            String commandString = commandTemplate
                    .replace("%name%", ownUseEntry.inventoryItem().getDisplayName())
                    .replace("%amount%", String.valueOf(ownUseEntry.amount()))
                    .replace("%purity%", String.valueOf(ownUseEntry.purity()));

            commandStrings.add(commandString);
        }

        if (commandStrings.isEmpty()) {
            sendModMessage("Du hast keinen Eigenbedarf gesetzt.", false);
        }

        return commandStrings;
    }
}
