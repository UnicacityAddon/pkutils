package de.rettichlp.pkutils.common.gui.overlay;

import lombok.Builder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.Supplier;

import static de.rettichlp.pkutils.PKUtilsClient.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_PADDING;

@Builder
public class TextOverlay extends OverlayEntry {

    private final Supplier<Text> textSupplier;

    @Builder.Default
    private Color backgroundColor = new Color(127, 127, 127, 100);

    @Builder.Default
    private Color borderColor = new Color(255, 255, 255, 255);

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
        drawContext.drawTextWithShadow(this.textRenderer, this.textSupplier.get(), innerX + TEXT_BOX_PADDING, innerY + TEXT_BOX_PADDING, 0xFFFFFF);

        // debug: draw outline
        //drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 0, 255).getRGB());
    }
}
