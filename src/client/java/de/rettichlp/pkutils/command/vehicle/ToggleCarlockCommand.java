package de.rettichlp.pkutils.command.vehicle;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static de.rettichlp.pkutils.PKUtilsClient.storage;

@PKUtilsCommand(label = "togglecarlock")
public class ToggleCarlockCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node.executes(context -> {
            boolean newState = !storage.isCarLock();
            storage.setCarLock(newState);
            hudService.sendInfoNotification(newState ? "Automatische Fahrzeugverriegelung aktiviert" : "Automatische Fahrzeugverriegelung deaktiviert");
            return 1;
        });
    }
}