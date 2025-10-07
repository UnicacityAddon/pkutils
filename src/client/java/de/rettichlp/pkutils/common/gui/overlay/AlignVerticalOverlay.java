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
    public int getWidth() {
        int entryWidth = this.overlayEntries.stream().map(OverlayEntry::getWidth).max(Integer::compareTo).orElse(0);
        return entryWidth + 2 * TEXT_BOX_MARGIN; // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.overlayEntries.stream().map(OverlayEntry::getHeight).reduce(0, Integer::sum);
        return entryHeight + 2 * TEXT_BOX_MARGIN; // top + bottom margin
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, @NotNull AlignOverlay.DrawPosition drawPosition) {
        draw(drawContext, drawPosition.getX(getWidth()), drawPosition.getY(getHeight()), drawPosition.getAlignment());

        // debug: draw background
        //drawContext.fill(drawPosition.getX(getWidth()), drawPosition.getY(getHeight()), getWidth(), getHeight(), RED.getRGB());
    }

    public void draw(DrawContext drawContext, int x, int y, AlignOverlay.Alignment alignment) {
        int innerX = x + TEXT_BOX_MARGIN;
        int innerY = y + TEXT_BOX_MARGIN;

        int yOffset = innerY;

        for (OverlayEntry overlayEntry : this.overlayEntries) {
            overlayEntry.draw(drawContext, innerX, yOffset);
            yOffset += overlayEntry.getHeight();
        }
    }
}
