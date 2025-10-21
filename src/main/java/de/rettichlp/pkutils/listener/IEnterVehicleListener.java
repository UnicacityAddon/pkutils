package de.rettichlp.pkutils.listener;

import net.minecraft.entity.Entity;

public interface IEnterVehicleListener extends IPKUtilsListener {

    void onEnterVehicle(Entity vehicle);
}
