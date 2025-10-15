package de.rettichlp.pkutils.common.gui.overlay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.Alignment.CENTER;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.Alignment.LEFT;
import static de.rettichlp.pkutils.common.gui.overlay.OverlayEntry.Alignment.RIGHT;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

@Getter
public abstract class OverlayEntry {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment);

    public void draw(@NotNull DrawContext drawContext, @NotNull AlignOverlay.DrawPosition drawPosition) {
        draw(drawContext, drawPosition.getX(getWidth()), drawPosition.getY(getHeight()), drawPosition.getAlignment());
    }

    public int getContentWidth() {
        return getWidth() - TEXT_BOX_MARGIN * 2;
    }

    public int getContentHeight() {
        return getHeight() - TEXT_BOX_MARGIN * 2;
    }

    @Getter
    @AllArgsConstructor
    public enum DrawPosition {

        TOP_LEFT(LEFT),
        TOP_CENTER(CENTER),
        TOP_RIGHT(RIGHT),
        BOTTOM_LEFT(LEFT),
        BOTTOM_CENTER(CENTER),
        BOTTOM_RIGHT(RIGHT);

        private final Alignment alignment;

        public int getX(int width) {
            int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            return switch (this) {
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - width;
                case TOP_LEFT, BOTTOM_LEFT -> 0;
                case TOP_CENTER, BOTTOM_CENTER -> (scaledWidth - width) / 2;
            };
        }

        public int getY(int height) {
            int scaledHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
            return switch (this) {
                case TOP_RIGHT, TOP_LEFT, TOP_CENTER -> 0;
                case BOTTOM_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER -> scaledHeight - height;
            };
        }

        public int getXWithMargin(int width) {
            int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            return switch (this) {
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - (width + TEXT_BOX_MARGIN);
                case TOP_LEFT, BOTTOM_LEFT -> TEXT_BOX_MARGIN;
                case TOP_CENTER, BOTTOM_CENTER -> (scaledWidth - width) / 2;
            };
        }

        public int getYWithMargin(int height) {
            int scaledHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
            return switch (this) {
                case TOP_RIGHT, TOP_LEFT, TOP_CENTER -> TEXT_BOX_MARGIN;
                case BOTTOM_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER -> scaledHeight - height - TEXT_BOX_MARGIN;
            };
        }
    }

    public enum Alignment {

        LEFT,
        CENTER,
        RIGHT
    }
}
