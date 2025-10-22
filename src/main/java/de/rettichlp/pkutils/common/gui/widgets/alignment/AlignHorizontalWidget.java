package de.rettichlp.pkutils.common.gui.widgets.alignment;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.text.Text.empty;

public class AlignHorizontalWidget extends AlignWidget<AbstractPKUtilsWidget> {

    @Override
    public void add(AbstractPKUtilsWidget entry) {
        this.pkUtilsWidgets.add(entry);
    }

    @Override
    public Text getDisplayName() {
        return empty();
    }

    @Override
    public Text getTooltip() {
        return empty();
    }

    @Override
    public int getWidth() {
        return this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getWidth).reduce(0, Integer::sum);
    }

    @Override
    public int getHeight() {
        return this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getHeight).max(Integer::compareTo).orElse(0);
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, AbstractPKUtilsWidget.Alignment alignment) {
        int xOffset = x;

        for (AbstractPKUtilsWidget pkUtilsWidget : this.pkUtilsWidgets) {
            pkUtilsWidget.draw(drawContext, xOffset, y, alignment);
            xOffset += pkUtilsWidget.getWidth();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(255, 0, 0).getRGB());
        }
    }
}
