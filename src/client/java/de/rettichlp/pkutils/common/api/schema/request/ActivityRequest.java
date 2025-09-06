package de.rettichlp.pkutils.common.api.schema.request;

import de.rettichlp.pkutils.common.api.schema.ActivityType;
import lombok.Data;
import net.minecraft.client.MinecraftClient;

import java.net.URI;

import static java.net.URI.create;

@Data
public class ActivityRequest implements IRequest {

    private final String playerName;
    private final String playerUuid;
    private final ActivityType activity;

    public ActivityRequest(ActivityType activity) {
        // KORREKTUR: getSession() wurde durch .player.getGameProfile() ersetzt
        this.playerName = MinecraftClient.getInstance().getSession().getUsername();
        this.playerUuid = MinecraftClient.getInstance().player.getGameProfile().getId().toString();
        this.activity = activity;
    }

    @Override
    public URI getUrl() {
        return create("https://activitycheck.pkutils.eu/proxy");
    }
}