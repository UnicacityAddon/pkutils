package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.OwnUseEntry;
import de.rettichlp.pkutils.common.models.config.MainConfig;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static de.rettichlp.pkutils.PKUtilsClient.*;
import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "eigenbedarf")
public class EigenbedarfCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("set")
                        .then(argument("koks/gras", word())
                                .then(argument("reinheit", integer(0, 3))
                                        .then(argument("anzahl", integer(1, MAX_VALUE))
                                                .executes(context -> {
                                                    String drug = getString(context, "koks/gras");
                                                    int purity = getInteger(context, "reinheit");
                                                    int amount = getInteger(context, "anzahl");
                                                    if (!(drug.equalsIgnoreCase("koks") || drug.equalsIgnoreCase("gras"))) {
                                                        sendModMessage("Gebe eine gültige Droge an", false);
                                                        return 1;
                                                    }

                                                    /*
                                                     * Edit existing entry
                                                     */
                                                    MainConfig config = configService.load();
                                                    List<OwnUseEntry> supplies = config.getSupplies();
                                                    if (supplies.stream().anyMatch(ownUseEntry -> ownUseEntry.drug().equalsIgnoreCase(drug))) {
                                                        supplies.stream()
                                                                .filter(ownUseEntry -> ownUseEntry.drug().equalsIgnoreCase(drug))
                                                                .findFirst()
                                                                .ifPresent(ownUseEntry -> {
                                                                    configService.edit(mainConfig -> mainConfig.getSupplies().remove(ownUseEntry));
                                                                });

                                                        OwnUseEntry ownUseEntry = new OwnUseEntry(drug, purity, amount);
                                                        configService.edit(mainConfig -> mainConfig.getSupplies().add(ownUseEntry));
                                                        sendModMessage("Eigenbedarf aktualisiert -> " + drug + " | " + purity + "er | " + amount + "x", false);
                                                        return 1;
                                                    }

                                                    /*
                                                     * Add new entry
                                                     */
                                                    OwnUseEntry ownUseEntry = new OwnUseEntry(drug, purity, amount);
                                                    configService.edit(mainConfig -> mainConfig.getSupplies().add(ownUseEntry));
                                                    sendModMessage("Eigenbedarf gesetzt -> " + drug + " | " + purity + "er | " + amount + "x", false);
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

                            MainConfig mainConfig = configService.load();
                            if (mainConfig.getSupplies().isEmpty()) {
                                sendModMessage("Du hast keinen Eigenbedarf gesetzt. Setze diesen mit /eigenbedarf set <koks/gras> <reinheit> <anzahl>", false);
                                return 1;
                            }

                            List<OwnUseEntry> supplies = mainConfig.getSupplies();
                            AtomicInteger counter = new AtomicInteger(0);
                            supplies.forEach(ownUseEntry -> {
                                long delay = SECONDS.toMillis(counter.incrementAndGet());
                                delayedAction(() -> networkHandler.sendChatCommand(
                                        "selldrug  " + targetPlayer + " "
                                                + ownUseEntry.drug() + " "
                                                + ownUseEntry.amount() + " "
                                                + ownUseEntry.purity() + " "
                                                + "1"
                                ), delay);
                            });

                            return 1;
                        })))

                .executes(context -> {
                    MainConfig mainConfig = configService.load();
                    if (mainConfig.getSupplies().isEmpty()) {
                        sendModMessage("Du hast keinen Eigenbedarf gesetzt. Setze diesen mit /eigenbedarf set <koks/gras> <reinheit> <anzahl>", false);
                        return 1;
                    }

                    List<OwnUseEntry> supplies = mainConfig.getSupplies();
                    AtomicInteger counter = new AtomicInteger(0);
                    supplies.forEach(ownUseEntry -> {
                        long delay = SECONDS.toMillis(counter.incrementAndGet());
                        delayedAction(() -> networkHandler.sendChatCommand(
                                "dbank get "
                                        + ownUseEntry.drug() + " "
                                        + ownUseEntry.purity() + " "
                                        + ownUseEntry.amount()
                        ), delay);
                    });

                    return 1;
                });

    }
}
