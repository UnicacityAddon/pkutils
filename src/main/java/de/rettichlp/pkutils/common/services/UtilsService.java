package de.rettichlp.pkutils.common.services;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtils.MOD_ID;

public class UtilsService {

    public void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                MinecraftClient.getInstance().execute(runnable);
            }
        }, milliseconds);
    }

    public String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new NullPointerException("Cannot find version"));
    }
}
