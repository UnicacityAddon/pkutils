package de.rettichlp.pkutils.common.gui.widgets.alignment;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignVerticalWidget extends AlignWidget<AbstractPKUtilsWidget> {

    @Override
    public void add(AbstractPKUtilsWidget entry) {
        this.pkUtilsWidgets.add(entry);
    }

    @Override
    public int getWidth() {
        int entryWidth = this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getWidth).max(Integer::compareTo).orElse(0);
        return entryWidth + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getHeight).reduce(0, Integer::sum);
        return entryHeight + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // top + bottom margin
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, AbstractPKUtilsWidget.Alignment alignment) {
        int innerX = x + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);
        int innerY = y + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);

        int yOffset = innerY;

        for (AbstractPKUtilsWidget pkUtilsWidget : this.pkUtilsWidgets) {
            // apply alignment
            int alignmentXModifier = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> (getContentWidth() - pkUtilsWidget.getWidth()) / 2;
                case RIGHT -> getContentWidth() - pkUtilsWidget.getWidth();
            };

            pkUtilsWidget.draw(drawContext, innerX + alignmentXModifier, yOffset, alignment);
            yOffset += pkUtilsWidget.getHeight();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 255, 0).getRGB());
        }
    }
}
