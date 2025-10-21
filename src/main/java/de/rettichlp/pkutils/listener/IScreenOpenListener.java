package de.rettichlp.pkutils.listener;

import net.minecraft.client.gui.screen.Screen;

public interface IScreenOpenListener extends IPKUtilsListener {

    void onScreenOpen(Screen screen, int scaledWidth, int scaledHeight);
}
