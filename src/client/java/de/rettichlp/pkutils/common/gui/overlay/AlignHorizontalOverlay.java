package de.rettichlp.pkutils.common.gui.overlay;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignHorizontalOverlay extends AlignOverlay<OverlayEntry> {

    @Override
    public void add(OverlayEntry entry) {
        this.overlayEntries.add(entry);
    }

    @Override
    public int getWidth() {
        int entryWidth = this.overlayEntries.stream().map(OverlayEntry::getWidth).reduce(0, Integer::sum);
        return entryWidth + 2 * TEXT_BOX_MARGIN; // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.overlayEntries.stream().map(OverlayEntry::getHeight).max(Integer::compareTo).orElse(renderService.getTextBoxSizeY());
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

        int xOffset = innerX;

        for (OverlayEntry overlayEntry : this.overlayEntries) {
            overlayEntry.draw(drawContext, xOffset, innerY);
            xOffset += overlayEntry.getWidth();
        }
    }
}
