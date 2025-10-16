package de.rettichlp.pkutils.common.gui.overlay;

import lombok.Builder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.function.Supplier;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_PADDING;
import static java.awt.Color.WHITE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;

@Builder
public class ProgressTextOverlay extends OverlayEntry {

    private final Supplier<Text> textSupplier;
    private double progress;

    @Builder.Default
    private Color backgroundColor = renderService.getSecondaryColor(WHITE);

    @Builder.Default
    private Color borderColor = WHITE;

    @Override
    public int getWidth() {
        Text text = this.textSupplier.get();
        return renderService.getTextBoxSizeX(text);
    }

    @Override
    public int getHeight() {
        return renderService.getTextBoxSizeY();
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        int innerX = x + TEXT_BOX_MARGIN;
        int innerY = y + TEXT_BOX_MARGIN;

        drawContext.fill(innerX, innerY, innerX + getContentWidth(), innerY + getContentHeight(), this.backgroundColor.getRGB());
        drawContext.drawBorder(innerX, innerY, getContentWidth(), getContentHeight(), this.borderColor.getRGB());
        drawContext.drawHorizontalLine(innerX + 2, (int) (innerX + ((getContentWidth() - 4) * this.progress)), innerY + getContentHeight() - 3, this.borderColor.getRGB());
        drawContext.drawTextWithShadow(this.textRenderer, this.textSupplier.get(), innerX + TEXT_BOX_PADDING, innerY + TEXT_BOX_PADDING, 0xFFFFFF);

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 0, 255).getRGB());
        }
    }

    public static double calculateProgress(Temporal startTime, long duration) {
        long elapsedMillis = between(startTime, now()).toMillis();
        double progress = (double) elapsedMillis / duration;
        return min(1.0, max(0.0, progress));
    }
}
