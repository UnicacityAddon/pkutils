package de.rettichlp.pkutils.listener;

import net.minecraft.text.Text;

public interface IMessageReceiveListener extends IPKUtilsListener {

    boolean onMessageReceive(Text text, String message);
}
