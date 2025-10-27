package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.gui.screens.FactionScreen;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.FactionMember;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.screens.FactionScreen.SortingType.RANK;
import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.DESCENDING;

@PKUtilsCommand(label = "activity")
public class ActivityCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node.executes(context -> {
            Faction faction = storage.getFaction(player.getGameProfile().getName());
            api.getActivityResetTime(faction, weeklyTime -> {
                MinecraftClient client = MinecraftClient.getInstance();

                LocalDateTime to = weeklyTime.nextOccurrence();
                LocalDateTime from = to.minusWeeks(1);
                api.getActivityPlayers(from, to, faction.getMembers().stream().map(FactionMember::playerName).toList(), activities -> client.execute(() -> {
                    FactionScreen factionScreen = new FactionScreen(faction, RANK, DESCENDING, activities, from, to, 0);
                    client.setScreen(factionScreen);
                }));
            });

            return 1;
        });
    }
}
