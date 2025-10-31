package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.listener.impl.EventListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.messageService;
import static de.rettichlp.pkutils.PKUtils.player;
import static net.minecraft.text.Text.empty;

@PKUtilsCommand(label = "notvisitedhouses")
public class NotVisitedHousesCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    List<Integer> doorNumbersVisited = configuration.getHalloweenClickedDoors().stream()
                            .map(EventListener.HalloweenDoor::getDoorNumber)
                            .toList();

                    Collection<Integer> doorNumbersNotVisited = new ArrayList<>();
                    for (int i = 1; i <= 828; i++) {
                        if (!doorNumbersVisited.contains(i)) {
                            doorNumbersNotVisited.add(i);
                        }
                    }

                    player.sendMessage(empty(), false);

                    messageService.sendModMessage("Noch nicht besuchte Häuser:", false);
                    doorNumbersNotVisited.forEach(integer -> messageService.sendModMessage(Text.of("- Haus " + integer), false));

                    player.sendMessage(empty(), false);

                    return 1;
                });
    }
}
