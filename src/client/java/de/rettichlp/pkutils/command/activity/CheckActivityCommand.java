package de.rettichlp.pkutils.command.activity;

import com.google.gson.Gson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.api.schema.request.ViewTokenRequest;
import de.rettichlp.pkutils.common.api.schema.response.ViewTokenResponse;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.ClickEvent.Action.OPEN_URL;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;

@PKUtilsCommand(label = "checkactivity")
public class CheckActivityCommand extends CommandBase {

    private static final String BASE_URL = "https://activitycheck.pkutils.eu";
    private static final Gson GSON = new Gson();

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("player", word())
                        .suggests((context, builder) -> {
                            Faction faction = storage.getFaction(player.getName().getString());
                            return faction == NULL ? empty() : suggestMatching(faction.getMembers().stream()
                                    .map(FactionMember::playerName), builder);
                        })
                        .executes(context -> {
                            String playerName = player.getName().getString();
                            Faction faction = storage.getFaction(playerName);

                            String targetName = getString(context, "player");
                            Faction targetFaction = storage.getFaction(targetName);

                            boolean isNotSuperUser = !"25855f4d-3874-4a7f-a6ad-e9e4f3042e19".equals(player.getUuidAsString());

                            if (isNotSuperUser && faction != targetFaction) {
                                sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                return 1;
                            }

                            if (isNotSuperUser && targetFaction == NULL) {
                                sendModMessage("Der Spieler ist in keiner Fraktion.", false);
                                return 1;
                            }

                            if (isNotSuperUser && storage.getFactionMembers(faction).stream()
                                    .filter(factionMember -> factionMember.playerName().equals(playerName))
                                    .findFirst()
                                    .map(factionMember -> factionMember.rank() < 4)
                                    .orElse(true)) {
                                sendModMessage("Du musst Rang 4 oder höher sein, um die Aktivitäten von anderen Mitgliedern einsehen zu können.", false);
                                return 1;
                            }

                            requestAndOpenActivityLink(targetName);
                            return 1;
                        })
                )
                .executes(context -> {
                    String playerName = player.getName().getString();
                    requestAndOpenActivityLink(playerName);
                    return 1;
                });
    }

    private void requestAndOpenActivityLink(String playerName) {
        Request<ViewTokenRequest> request = Request.<ViewTokenRequest>builder()
                .body(new ViewTokenRequest(playerName))
                .build();

        request.send(response -> {
            ViewTokenResponse viewTokenResponse = GSON.fromJson(response.body(), ViewTokenResponse.class);

            if (response.statusCode() >= 400 || viewTokenResponse.getAccessToken() == null) {
                String errorMessage = viewTokenResponse.getError() != null ? viewTokenResponse.getError() : "Unbekannter Fehler.";
                sendModMessage("Fehler: " + errorMessage, true);
                return;
            }

            String personalUrl = BASE_URL + "/user/" + playerName + "?token=" + viewTokenResponse.getAccessToken();

            MutableText linkText = modMessagePrefix.copy()
                    .append(of("Klicke hier, um die Aktivitäten von ")).append(of(playerName).copy().formatted(Formatting.BLUE, Formatting.UNDERLINE)).append(of(" anzuzeigen."));

            MutableText clickableLinkText = linkText.styled(style -> style
                    .withClickEvent(new ClickEvent(OPEN_URL, personalUrl))
                    .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Öffnet einen gesicherten Link")))
            );

            player.sendMessage(clickableLinkText, false);

        }, throwable -> sendModMessage("Fehler: Konnte keinen Zugriffs-Token für " + playerName + " erhalten.", true));
    }
}