package de.rettichlp.ucutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.ucutils.common.registry.CommandBase;
import de.rettichlp.ucutils.common.registry.UCUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.StringJoiner;

import static de.rettichlp.ucutils.UCUtils.MOD_ID;
import static de.rettichlp.ucutils.UCUtils.messageService;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.UCUtils.syncService;
import static de.rettichlp.ucutils.UCUtils.utilService;
import static java.net.URI.create;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.TextColor.DARK_GRAY;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.WHITE;

@UCUtilsCommand(label = "ucutils")
public class ModCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(literal("sync")
                        .executes(context -> {
                            syncService.syncFactionMembers();
                            syncService.syncTeamMembers();
                            syncService.checkForUpdates();

                            utilService.delayedAction(() -> {
                                storage.getPlayerFactionCache().clear();
                                syncService.syncFactionSpecificData();
                            }, 2000);

                            return 1;
                        }))
                .executes(context -> {
                    String version = utilService.getVersion();
                    String authors = getAuthors();
                    String contributors = getContributors();

                    player.sendSystemMessage(empty());

                    messageService.sendModMessage("UCUtils Version " + version, false);

                    messageService.sendModMessage(empty()
                            .append(Component.literal("Authors").withColor(GRAY))
                            .append(Component.literal(":").withColor(DARK_GRAY)).append(" ")
                            .append(Component.literal(authors).withColor(WHITE)), false);

                    messageService.sendModMessage(empty()
                            .append(Component.literal("Discord").withColor(GRAY))
                            .append(Component.literal(":").withColor(DARK_GRAY)).append(" ")
                            .append(Component.literal("https://discord.gg/mZGAAwhPHu").withStyle(style -> style
                                    .withColor(WHITE)
                                    .withClickEvent(new ClickEvent.OpenUrl(create("https://discord.gg/mZGAAwhPHu"))))), false);

                    messageService.sendModMessage(empty()
                            .append(Component.literal("GitHub").withColor(GRAY))
                            .append(Component.literal(":").withColor(DARK_GRAY)).append(" ")
                            .append(Component.literal("https://github.com/UnicacityAddon/ucutils").withStyle(style -> style
                                    .withColor(WHITE)
                                    .withClickEvent(new ClickEvent.OpenUrl(create("https://github.com/UnicacityAddon/ucutils"))))), false);

                    messageService.sendModMessage(empty()
                            .append(Component.literal("Contributors").withColor(GRAY))
                            .append(Component.literal(":").withColor(DARK_GRAY)).append(" ")
                            .append(Component.literal(contributors).withColor(WHITE)), false);

                    player.sendSystemMessage(empty());

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

    private String getContributors() {
        Collection<Person> contributors = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getContributors())
                .orElseThrow(() -> new NullPointerException("Cannot find contributors"));

        StringJoiner stringJoiner = new StringJoiner(", ");
        contributors.forEach(person -> stringJoiner.add(person.getName()));

        return stringJoiner.toString();
    }
}
