package de.rettichlp.pkutils.common.gui.overlay;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignVerticalOverlay extends AlignOverlay<OverlayEntry> {

    @Override
    public void add(OverlayEntry entry) {
        this.overlayEntries.add(entry);
    }

    @Override
    public int getWidth() {
        int entryWidth = this.overlayEntries.stream().map(OverlayEntry::getWidth).max(Integer::compareTo).orElse(0);
        return entryWidth + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.overlayEntries.stream().map(OverlayEntry::getHeight).reduce(0, Integer::sum);
        return entryHeight + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // top + bottom margin
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        int innerX = x + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);
        int innerY = y + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);

        int yOffset = innerY;

        for (OverlayEntry overlayEntry : this.overlayEntries) {
            // apply alignment
            int alignmentXModifier = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> (getContentWidth() - overlayEntry.getWidth()) / 2;
                case RIGHT -> getContentWidth() - overlayEntry.getWidth();
            };

            overlayEntry.draw(drawContext, innerX + alignmentXModifier, yOffset, alignment);
            yOffset += overlayEntry.getHeight();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 255, 0).getRGB());
        }
    }
}
