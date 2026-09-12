package de.rettichlp.ucutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.ucutils.common.models.ScreenshotType;
import de.rettichlp.ucutils.common.registry.CommandBase;
import de.rettichlp.ucutils.common.registry.UCUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.ucutils.UCUtils.messageService;
import static de.rettichlp.ucutils.UCUtils.player;
import static java.lang.String.valueOf;
import static java.nio.file.Files.list;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.TextColor.AQUA;
import static net.minecraft.network.chat.TextColor.DARK_AQUA;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;

@UCUtilsCommand(label = "screenshot")
public class ScreenshotCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("screenshotType", greedyString())
                        .suggests((context, builder) -> suggest(stream(ScreenshotType.values()).map(ScreenshotType::getDisplayName).toList(), builder))
                        .executes(context -> {
                            // placeholder method: implemented as mixin in ChatScreenMixin
                            return 1;
                        }))
                .executes(context -> {
                    player.sendSystemMessage(empty());
                    messageService.sendModMessage("Screenshots:", false);

                    for (ScreenshotType screenshotType : ScreenshotType.values()) {
                        File screenshotDirectory = screenshotType.getScreenshotDirectory();

                        long fileCount;
                        try (Stream<Path> files = list(screenshotDirectory.toPath())) {
                            fileCount = files.count();
                        } catch (IOException e) {
                            fileCount = 0;
                        }

                        messageService.sendModMessage(empty()
                                .append(Component.literal(screenshotType.getDisplayName()).withColor(GRAY))
                                .append(Component.literal(":").withColor(DARK_GRAY)).append(SPACE)
                                .append(Component.literal(valueOf(fileCount)))
                                .append(Component.literal(" ↗").withStyle(style -> style
                                        .withColor(AQUA)
                                        .withBold(true)
                                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Ordner öffnen").withColor(DARK_AQUA)))
                                        .withClickEvent(new ClickEvent.OpenFile(screenshotDirectory.getAbsolutePath())))), false);
                    }

                    player.sendSystemMessage(empty());
                    return 1;
                });
    }
}
