package de.rettichlp.pkutils.common.models;

import de.rettichlp.pkutils.common.gui.options.ShutdownScreen;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtils.storage;

@Getter
@AllArgsConstructor
public enum ShutdownReason {

    CEMETERY(1, "Friedhof"),
    JAIL(2, "Gefängnis");

    private final int priority;
    private final String displayName;

    public void activate() {
        storage.getActiveShutdowns().add(this);

        // run later to avoid conflicts with the chat screen closing after command execution
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> client.setScreen(new ShutdownScreen(ShutdownReason.this)));
            }
        }, 100);
    }
}
