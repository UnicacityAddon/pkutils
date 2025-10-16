package de.rettichlp.pkutils.common.gui.screen.buttons;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public interface IButtonEntry {

    Text getDisplayName();

    Tooltip getTooltip();
}
