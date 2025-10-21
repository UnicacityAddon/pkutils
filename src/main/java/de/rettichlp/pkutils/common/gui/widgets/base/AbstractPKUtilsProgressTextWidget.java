package de.rettichlp.pkutils.common.gui.widgets.base;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.temporal.Temporal;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_PADDING;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;

public abstract class AbstractPKUtilsProgressTextWidget<C extends PKUtilsWidgetConfiguration> extends AbstractPKUtilsTextWidget<C> {

    public abstract Text text();

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        drawContext.fill(x, y, x + getWidth(), y + getHeight(), getBackgroundColor().getRGB());
        drawContext.drawBorder(x, y, getWidth(), getHeight(), getBorderColor().getRGB());
        drawContext.drawText(getTextRenderer(), text(), x + TEXT_BOX_PADDING, y + TEXT_BOX_PADDING, 0xFFFFFF, false);

        int maxProgressWidth = getWidth() - TEXT_BOX_PADDING * 2;
        int xProgressStart = (int) (x + TEXT_BOX_PADDING + maxProgressWidth * progress());
        int xProgressEnd = x + getWidth() - TEXT_BOX_PADDING;

        drawContext.drawHorizontalLine(xProgressStart, xProgressEnd, y + getHeight() - 3, getBorderColor().getRGB());

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 0, 255).getRGB());
        }
    }

    public abstract double progress();

    protected double calculateProgress(Temporal creationTime, long durationInMillis) {
        long elapsedMillis = between(creationTime, now()).toMillis();
        double progress = (double) elapsedMillis / durationInMillis;
        return min(1.0, max(0.0, progress));
    }
}
