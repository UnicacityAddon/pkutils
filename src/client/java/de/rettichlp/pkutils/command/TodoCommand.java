package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.config.MainConfig;
import de.rettichlp.pkutils.common.models.config.TodoEntry;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.WHITE;

@PKUtilsCommand(label = "todo")
public class TodoCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("add")
                        .then(argument("task", greedyString())
                                .executes(context -> {
                                    String taskString = getString(context, "task");
                                    TodoEntry todoEntry = new TodoEntry(taskString);

                                    configService.edit(mainConfig -> mainConfig.getTodos().add(todoEntry));

                                    sendTodoList();
                                    return 1;
                                })))
                .then(literal("delete")
                        .then(argument("id", greedyString())
                                .executes(context -> {
                                    String id = getString(context, "id");

                                    configService.edit(mainConfig -> mainConfig.getTodos()
                                            .removeIf(todoEntry -> todoEntry.getCreatedAt().toString().equals(id)));

                                    sendTodoList();
                                    return 1;
                                })))
                .then(literal("done")
                        .then(argument("id", greedyString())
                                .executes(context -> {
                                    String id = getString(context, "id");

                                    configService.edit(mainConfig -> mainConfig.getTodos().stream()
                                            .filter(todoEntry -> todoEntry.getCreatedAt().toString().equals(id))
                                            .findFirst()
                                            .ifPresent(todoEntry -> todoEntry.setDone(true)));

                                    sendTodoList();
                                    return 1;
                                })))
                .executes(context -> {
                    sendTodoList();
                    return 1;
                });
    }

    private void sendTodoList() {
        MainConfig mainConfig = configService.load();
        List<TodoEntry> todos = mainConfig.getTodos();

        player.sendMessage(empty(), false);
        sendModMessage("TODOs:", false);
        todos.forEach(todoEntry -> sendModMessage(empty()
                .append(todoEntry.isDone() ? todoEntry.getDeleteButton() : todoEntry.getDoneButton()).append(" ")
                .append(of(todoEntry.getTask()).copy().styled(style -> style.withColor(WHITE).withStrikethrough(todoEntry.isDone()))), false));
        player.sendMessage(empty(), false);
    }
}
