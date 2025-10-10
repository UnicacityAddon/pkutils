package de.rettichlp.pkutils.common.gui.overlay;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignHorizontalOverlay extends AlignOverlay<OverlayEntry> {

    @Override
    public void add(OverlayEntry entry) {
        this.overlayEntries.add(entry);
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        int innerX = x + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);
        int innerY = y + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);

        int xOffset = innerX;

        for (OverlayEntry overlayEntry : this.overlayEntries) {
            overlayEntry.draw(drawContext, xOffset, innerY, alignment);
            xOffset += overlayEntry.getWidth();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(255, 0, 0).getRGB());
        }
    }

    @Override
    public int getWidth() {
        int entryWidth = this.overlayEntries.stream().map(OverlayEntry::getWidth).reduce(0, Integer::sum);
        return entryWidth + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.overlayEntries.stream().map(OverlayEntry::getHeight).max(Integer::compareTo).orElse(renderService.getTextBoxSizeY());
        return entryHeight + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // top + bottom margin
    }
}
