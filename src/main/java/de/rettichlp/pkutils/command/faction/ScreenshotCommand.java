package de.rettichlp.pkutils.command.faction;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.ScreenshotType;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static de.rettichlp.pkutils.PKUtils.player;
import static java.lang.String.valueOf;
import static java.nio.file.Files.list;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.ClickEvent.Action.OPEN_FILE;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

@PKUtilsCommand(label = "screenshot")
public class ScreenshotCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("type")
                        .then(argument("screenshotType", greedyString())
                                .suggests((context, builder) -> suggestMatching(stream(ScreenshotType.values()).map(ScreenshotType::getDisplayName).toList(), builder))
                                .executes(context -> {
                                    // placeholder method: implemented as mixin in ChatScreenMixin
                                    return 1;
                                })))
                .executes(context -> {
                    player.sendMessage(empty(), false);
                    sendModMessage("Screenshots:", false);

                    for (ScreenshotType screenshotType : ScreenshotType.values()) {
                        File screenshotDirectory = screenshotType.getScreenshotDirectory();

                        long fileCount;
                        try (Stream<Path> files = list(screenshotDirectory.toPath())) {
                            fileCount = files.count();
                        } catch (IOException e) {
                            fileCount = 0;
                        }

                        sendModMessage(empty()
                                .append(of(screenshotType.getDisplayName()).copy().formatted(GRAY))
                                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                                .append(of(valueOf(fileCount)))
                                .append(fileCount <= 0 ? empty() : of(" ↗").copy().styled(style -> style
                                        .withColor(AQUA)
                                        .withBold(true)
                                        .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Ordner öffnen").copy().formatted(DARK_AQUA)))
                                        .withClickEvent(new ClickEvent(OPEN_FILE, screenshotDirectory.getAbsolutePath())))), false);
                    }

                    player.sendMessage(empty(), false);
                    return 1;
                });
    }
}
