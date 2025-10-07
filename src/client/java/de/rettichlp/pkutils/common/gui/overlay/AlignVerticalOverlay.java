package de.rettichlp.pkutils.common.gui.overlay;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignVerticalOverlay extends AlignOverlay<OverlayEntry> {

    @Override
    public void add(OverlayEntry entry) {
        this.overlayEntries.add(entry);
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        int innerX = x + TEXT_BOX_MARGIN;
        int innerY = y + TEXT_BOX_MARGIN;

        int yOffset = innerY;

        for (OverlayEntry overlayEntry : this.overlayEntries) {
            overlayEntry.draw(drawContext, innerX, yOffset, alignment);
            yOffset += overlayEntry.getHeight();
        }

        // debug: draw background
        //drawContext.fill(x, y, getWidth(), getHeight(), new Color(0, 255, 0, 100).getRGB());
    }

    @Override
    public int getWidth() {
        int entryWidth = this.overlayEntries.stream().map(OverlayEntry::getWidth).max(Integer::compareTo).orElse(0);
        return entryWidth + 2 * TEXT_BOX_MARGIN; // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.overlayEntries.stream().map(OverlayEntry::getHeight).reduce(0, Integer::sum);
        return entryHeight + 2 * TEXT_BOX_MARGIN; // top + bottom margin
    }
}
