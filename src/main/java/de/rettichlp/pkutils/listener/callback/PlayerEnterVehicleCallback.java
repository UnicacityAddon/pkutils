package de.rettichlp.pkutils.listener.callback;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface PlayerEnterVehicleCallback {

    Event<PlayerEnterVehicleCallback> EVENT = createArrayBacked(PlayerEnterVehicleCallback.class, listeners -> vehicle -> {
        for (PlayerEnterVehicleCallback listener : listeners) {
            listener.onEnter(vehicle);
        }
    });

    void onEnter(Entity vehicle);
}
