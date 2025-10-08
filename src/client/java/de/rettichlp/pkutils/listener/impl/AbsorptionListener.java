package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.time.Duration.ofMinutes;

@PKUtilsListener
public class AbsorptionListener extends PKUtilsBase implements IAbsorptionGetListener {

    @Override
    public void onAbsorptionGet() {
        storage.getCountdowns().add(new Countdown("Absorption", ofMinutes(3)));
    }
}
