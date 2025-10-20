package de.rettichlp.pkutils.common.gui.widgets.base;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_PADDING;
import static java.awt.Color.WHITE;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

public abstract class AbstractPKUtilsTextWidget<C extends PKUtilsWidgetConfiguration> extends AbstractPKUtilsWidget<C> {

    public abstract Text text();

    @Override
    public int getWidth() {
        int fontWidth = getTextRenderer().getWidth(text());
        return fontWidth + 2 * TEXT_BOX_PADDING;
    }

    @Override
    public int getHeight() {
        return getTextRenderer().fontHeight + 2 * TEXT_BOX_PADDING;
    }

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment) {
        drawContext.fill(x, y, x + getWidth(), y + getHeight(), getBackgroundColor().getRGB());
        drawContext.drawBorder(x, y, getWidth(), getHeight(), getBorderColor().getRGB());
        drawContext.drawText(getTextRenderer(), text(), x + TEXT_BOX_PADDING, y + TEXT_BOX_PADDING, 0xFFFFFF, false);

        // debug: draw outline
        if (renderService.isDebugEnabled()) {
            drawContext.drawBorder(x, y, getWidth(), getHeight(), new Color(0, 0, 255).getRGB());
        }
    }

    public Color getBorderColor() {
        return WHITE;
    }

    public Color getBackgroundColor() {
        return renderService.getSecondaryColor(getBorderColor());
    }

    protected MutableText keyValue(String key, String value) {
        return keyValue(key, of(value));
    }

    protected MutableText keyValue(String key, Text value) {
        return empty()
                .append(of(key).copy().formatted(GRAY))
                .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                .append(value);
    }
}
