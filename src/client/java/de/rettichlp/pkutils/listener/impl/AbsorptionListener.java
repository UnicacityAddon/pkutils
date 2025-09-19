package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.Storage.Countdown.ABSORPTION;
import static java.time.LocalDateTime.now;

@PKUtilsListener
public class AbsorptionListener extends PKUtilsBase implements IAbsorptionGetListener {

    @Override
    public void onAbsorptionGet() {
        storage.getCountdowns().put(ABSORPTION, now());
    }
}
