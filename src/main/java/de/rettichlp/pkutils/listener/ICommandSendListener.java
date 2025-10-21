package de.rettichlp.pkutils.listener;

import org.jetbrains.annotations.NotNull;

public interface ICommandSendListener extends IPKUtilsListener {

    boolean onCommandSend(@NotNull String command);
}
