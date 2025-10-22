package de.rettichlp.pkutils.common.gui.widgets.alignment;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static net.minecraft.text.Text.empty;

public class AlignVerticalWidget extends AlignWidget<AbstractPKUtilsWidget> {

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
        return this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getWidth).max(Integer::compareTo).orElse(0);
    }

    @Override
    public int getHeight() {
        return this.pkUtilsWidgets.stream().map(AbstractPKUtilsWidget::getHeight).reduce(0, Integer::sum);
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, AbstractPKUtilsWidget.Alignment alignment) {
        int yOffset = y;

        for (AbstractPKUtilsWidget pkUtilsWidget : this.pkUtilsWidgets) {
            // apply alignment
            int alignmentXModifier = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> (getWidth() - pkUtilsWidget.getWidth()) / 2;
                case RIGHT -> getWidth() - pkUtilsWidget.getWidth();
            };

            pkUtilsWidget.draw(drawContext, x + alignmentXModifier, yOffset, alignment);
            yOffset += pkUtilsWidget.getHeight();
        }

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 255, 0).getRGB());
        }
    }
}
