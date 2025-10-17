package de.rettichlp.pkutils.common.gui.widgets.alignment;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public class AlignHorizontalWidget extends AlignWidget<AbstractPKUtilsWidget> {

    @Override
    public void add(AbstractPKUtilsWidget entry) {
        this.pkUtilsWidgets.add(entry);
    }

    @Override
    public int getWidth() {
        int entryWidth = this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getWidth).reduce(0, Integer::sum);
        return entryWidth + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // left + right margin
    }

    @Override
    public int getHeight() {
        int entryHeight = this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getHeight).max(Integer::compareTo).orElse(renderService.getTextBoxSizeY());
        return entryHeight + (this.disableMargin ? 0 : 2 * TEXT_BOX_MARGIN); // top + bottom margin
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, AbstractPKUtilsWidget.Alignment alignment) {
        int innerX = x + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);
        int innerY = y + (this.disableMargin ? 0 : TEXT_BOX_MARGIN);

        int xOffset = innerX;

        for (AbstractPKUtilsWidget pkUtilsWidget : this.pkUtilsWidgets) {
            pkUtilsWidget.draw(drawContext, xOffset, innerY, alignment);
            xOffset += pkUtilsWidget.getWidth();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(255, 0, 0).getRGB());
        }
    }
}
