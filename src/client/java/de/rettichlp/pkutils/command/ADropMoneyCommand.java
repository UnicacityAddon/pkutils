package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;

@PKUtilsCommand(label = "adropmoney")
public class ADropMoneyCommand extends CommandBase {

    private int step = 0;

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            switch (ADropMoneyCommand.this.step++) {
                                case 1 -> sendCommand("bank abbuchen 16000");
                                case 2 -> sendCommand("dropmoney");
                                case 3 -> {
                                    sendCommand("bank einzahlen 16000");
                                    ADropMoneyCommand.this.step = 0;
                                    this.cancel();
                                }
                            }
                        }
                    }, 0, 1000);

                    return 1;
                });
    }
}
