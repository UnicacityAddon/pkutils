package de.rettichlp.pkutils.listener;

import net.minecraft.text.Text;

public interface IMessageReceiveListener {

    boolean onMessageReceive(Text text, String message);
}
