package de.rettichlp.pkutils.command;

import com.google.gson.Gson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.api.schema.request.LeaderActivityRequest;
import de.rettichlp.pkutils.common.api.schema.request.Request;
import de.rettichlp.pkutils.common.api.schema.response.LeaderActivityResponse;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static net.minecraft.text.ClickEvent.Action.OPEN_URL;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;

@PKUtilsCommand(label = "leadercheckactivity")
public class LeaderCheckActivity extends CommandBase {

    private static final String BASE_URL = "https://activitycheck.pkutils.eu";
    private static final Gson GSON = new Gson();
    private static final List<String> SUPERUSER_UUIDS = List.of("25855f4d-3874-4a7f-a6ad-e9e4f3042e19", "929bbc61-2f89-45cd-a351-84f439842832");

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node.executes(context -> {
            String playerName = player.getName().getString();
            String playerUuid = player.getUuidAsString();
            Faction faction = storage.getFaction(playerName);

            if (!SUPERUSER_UUIDS.contains(playerUuid)) {
                if (storage.getFactionMembers(faction).stream()
                        .filter(member -> member.getPlayerName().equals(playerName))
                        .findFirst()
                        .map(member -> member.getRank() < 5)
                        .orElse(true)) {
                    sendModMessage("Du musst Rang 5 oder höher sein, um diese Funktion zu nutzen.", false);
                    return 1;
                }
            }

            requestAndOpenDashboardLink();
            return 1;
        });
    }

    private void requestAndOpenDashboardLink() {
        Request<LeaderActivityRequest> request = Request.<LeaderActivityRequest>builder()
                .body(new LeaderActivityRequest())
                .build();

        request.send(response -> {
            LeaderActivityResponse tokenResponse = GSON.fromJson(response.body(), LeaderActivityResponse.class);

            if (response.statusCode() >= 400 || tokenResponse.getAccessToken() == null) {
                String error = tokenResponse.getError() != null ? tokenResponse.getError() : "Unbekannter Fehler.";
                sendModMessage("Fehler: " + error, true);
                return;
            }

            String dashboardUrl = BASE_URL + "/leadercheckactivity?token=" + tokenResponse.getAccessToken();

            MutableText linkText = modMessagePrefix.copy()
                    .append(of("Klicke hier, um das ").copy().formatted(Formatting.WHITE))
                    .append(of("Leader Dashboard").copy().formatted(Formatting.AQUA, Formatting.UNDERLINE))
                    .append(of(" zu öffnen.").copy().formatted(Formatting.WHITE));

            MutableText clickableLinkText = linkText.styled(style -> style
                    .withClickEvent(new ClickEvent(OPEN_URL, dashboardUrl))
                    .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Öffnet das Dashboard für deine Fraktion")))
            );

            player.sendMessage(clickableLinkText, false);
        }, throwable -> sendModMessage("Fehler: Konnte keinen Zugriffs-Token für das Dashboard erhalten.", true));
    }
}
