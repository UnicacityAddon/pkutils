package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.models.ActivityEntry;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.ClickEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.networkHandler;
import static de.rettichlp.pkutils.PKUtils.player;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.syncService;
import static java.lang.String.valueOf;
import static java.time.LocalDateTime.MIN;
import static java.util.Arrays.stream;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.ClickEvent.Action.OPEN_URL;
import static net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

@PKUtilsCommand(label = "pkutils")
public class ModCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("fakeActivity")
                        .requires(fabricClientCommandSource -> isSuperUser())
                        .then(argument("activityType", word())
                                .suggests((context, builder) -> {
                                    stream(ActivityEntry.Type.values()).forEach(activityType -> builder.suggest(activityType.name()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String activityTypeString = context.getArgument("activityType", String.class);
                                    stream(ActivityEntry.Type.values())
                                            .filter(activityType -> activityType.name().equals(activityTypeString.toUpperCase()))
                                            .findFirst()
                                            .ifPresent(api::trackActivity);

                                    return 1;
                                })))
                .then(literal("userinfo")
                        .requires(fabricClientCommandSource -> isSuperUser())
                        .then(argument("player", word())
                                .suggests((context, builder) -> {
                                    List<String> list = networkHandler.getPlayerList().stream()
                                            .map(PlayerListEntry::getProfile)
                                            .map(GameProfile::getName)
                                            .toList();
                                    return suggestMatching(list, builder);
                                })
                                .executes(context -> {
                                    String playerName = context.getArgument("player", String.class);
                                    api.getUserInfo(playerName).thenAccept(objectMap -> {
                                        String version = valueOf(objectMap.getOrDefault("version", "n/A"));

                                        player.sendMessage(empty(), false);

                                        sendModMessage("PKUtils User Information - " + playerName, false);

                                        sendModMessage(empty()
                                                .append(of("Version").copy().formatted(GRAY))
                                                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                                                .append(of(version).copy().formatted(WHITE)), false);

                                        sendModMessage(empty()
                                                .append(of("Aktivitäten").copy().formatted(GRAY))
                                                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                                                .append(of("Klick ↗").copy().styled(style -> style
                                                        .withColor(WHITE)
                                                        .withClickEvent(new ClickEvent(SUGGEST_COMMAND, "/activity player " + playerName)))), false);

                                        player.sendMessage(empty(), false);
                                    });

                                    return 1;
                                })))
                .executes(context -> {
                    String version = getVersion();
                    String authors = getAuthors();
                    LocalDateTime lastSyncTimestamp = syncService.getLastSyncTimestamp();

                    player.sendMessage(empty(), false);

                    sendModMessage("PKUtils Version " + version, false);

                    sendModMessage(empty()
                            .append(of("Autoren").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of(authors).copy().formatted(WHITE)), false);

                    sendModMessage(empty()
                            .append(of("Discord").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of("https://discord.gg/mZGAAwhPHu").copy().styled(style -> style
                                    .withColor(WHITE)
                                    .withClickEvent(new ClickEvent(OPEN_URL, "https://discord.gg/mZGAAwhPHu")))), false);

                    sendModMessage(empty()
                            .append(of("GitHub").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of("https://github.com/UnicacityAddon/pkutils").copy().styled(style -> style
                                    .withColor(WHITE)
                                    .withClickEvent(new ClickEvent(OPEN_URL, "https://github.com/UnicacityAddon/pkutils")))), false);

                    sendModMessage(empty()
                            .append(of("Letzte Synchronisierung").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of(lastSyncTimestamp.equals(MIN)
                                    ? "Nie"
                                    : dateTimeToFriendlyString(lastSyncTimestamp)).copy().formatted(WHITE)), false);

                    player.sendMessage(empty(), false);

                    storage.print();

                    return 1;
                });
    }

    private String getAuthors() {
        Collection<Person> authors = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getAuthors())
                .orElseThrow(() -> new NullPointerException("Cannot find authors"));

        StringJoiner stringJoiner = new StringJoiner(", ");
        authors.forEach(person -> stringJoiner.add(person.getName()));

        return stringJoiner.toString();
    }
}
