package de.rettichlp.pkutils.common.gui.overlay;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

@Getter
public abstract class OverlayEntry {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void draw(@NotNull DrawContext drawContext, int x, int y);

    public int getContentWidth() {
        return getWidth() - TEXT_BOX_MARGIN * 2;
    }

    public int getContentHeight() {
        return getHeight() - TEXT_BOX_MARGIN * 2;
    }
}
