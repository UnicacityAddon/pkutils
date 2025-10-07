package de.rettichlp.pkutils.common.gui.overlay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.rettichlp.pkutils.common.gui.overlay.AlignOverlay.Alignment.CENTER;
import static de.rettichlp.pkutils.common.gui.overlay.AlignOverlay.Alignment.LEFT;
import static de.rettichlp.pkutils.common.gui.overlay.AlignOverlay.Alignment.RIGHT;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

public abstract class AlignOverlay<T> extends OverlayEntry {

    protected final List<OverlayEntry> overlayEntries = new ArrayList<>();

    public abstract void add(T entry);

    public abstract void draw(@NotNull DrawContext drawContext, @NotNull DrawPosition drawPosition);

    @Override
    public void draw(@NotNull DrawContext drawContext, int x, int y) {}

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
        RIGHT;
    }
}
