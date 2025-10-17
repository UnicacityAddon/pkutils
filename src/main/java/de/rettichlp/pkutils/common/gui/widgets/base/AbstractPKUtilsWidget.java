package de.rettichlp.pkutils.common.gui.widgets.base;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.CENTER;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.LEFT;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.RIGHT;
import static de.rettichlp.pkutils.common.services.RenderService.TEXT_BOX_MARGIN;

@Getter
public abstract class AbstractPKUtilsWidget<C extends PKUtilsWidgetConfiguration> extends PKUtilsBase {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private int x;
    private int y;

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment);

    public void draw(@NotNull DrawContext drawContext) {
        if (!isVisible()) {
            return;
        }

        C configuration = getConfiguration();
        this.x = new Random().nextInt(0, 500);// configuration.getX();
        this.y = new Random().nextInt(0, 500);//configuration.getY();
        draw(drawContext, this.x, this.y, getAlignment());
    }

    public int getContentWidth() {
        return getWidth() - TEXT_BOX_MARGIN * 2;
    }

    public int getContentHeight() {
        return getHeight() - TEXT_BOX_MARGIN * 2;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        boolean mouseOverX = mouseX >= this.x && mouseX <= this.x + getWidth();
        boolean mouseOverY = mouseY >= this.y && mouseY <= this.y + getHeight();
        return mouseOverX && mouseOverY;
    }

    public boolean isVisible() {
        return true;
    }

    public C getConfiguration() {
        return null; // TODO
    }

    private Alignment getAlignment() {
        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int widthSegment = scaledWidth / 3;

        Alignment alignment;

        if (this.x <= widthSegment) {
            alignment = LEFT;
        } else if (this.x <= widthSegment * 2) {
            alignment = CENTER;
        } else {
            alignment = RIGHT;
        }

        return alignment;
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
