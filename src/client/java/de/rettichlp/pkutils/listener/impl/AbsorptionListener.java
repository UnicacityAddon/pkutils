package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IAbsorptionGetListener;

@PKUtilsListener
public class AbsorptionListener extends PKUtilsBase implements IAbsorptionGetListener {

    @Override
    public void onAbsorptionGet() {
        System.out.println("AbsorptionListener triggered");
    }
}
