package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static net.minecraft.client.render.RenderLayer.getLines;
import static net.minecraft.util.math.RotationAxis.POSITIVE_Y;
import static org.atteo.classindex.ClassIndex.getAnnotated;

public class RenderService extends PKUtilsBase {

    public static final int TEXT_BOX_PADDING = 3;
    public static final int TEXT_BOX_MARGIN = 2;

    @Getter
    private Set<AbstractPKUtilsWidget<?>> widgets = new HashSet<>();

    public int getTextBoxSizeX(StringVisitable text) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int fontWidth = textRenderer.getWidth(text);
        return fontWidth + 2 * TEXT_BOX_PADDING + 2 * TEXT_BOX_MARGIN;
    }

    public int getTextBoxSizeY() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.fontHeight + 2 * TEXT_BOX_PADDING + 2 * TEXT_BOX_MARGIN;
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void renderOutline(@NotNull MatrixStack matrices,
                              @NotNull VertexConsumerProvider vertexConsumers,
                              @NotNull EntityLike entity,
                              double expandBoundingBox) {
        Box box = entity.getBoundingBox().expand(expandBoundingBox);

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        float minX = (float) (box.minX - camX);
        float minY = (float) (box.minY - camY);
        float minZ = (float) (box.minZ - camZ);
        float maxX = (float) (box.maxX - camX);
        float maxY = (float) (box.maxY - camY);
        float maxZ = (float) (box.maxZ - camZ);

        VertexConsumer consumer = vertexConsumers.getBuffer(getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        drawLine(consumer, matrix, minX, minY, minZ, maxX, minY, minZ);
        drawLine(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ);
        drawLine(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ);
        drawLine(consumer, matrix, minX, minY, maxZ, minX, minY, minZ);

        drawLine(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ);
        drawLine(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ);
        drawLine(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ);
        drawLine(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ);

        drawLine(consumer, matrix, minX, minY, minZ, minX, maxY, minZ);
        drawLine(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ);
        drawLine(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ);
        drawLine(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ);
    }

    public void renderTextAboveEntity(@NotNull MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers,
                                      @NotNull Entity entity,
                                      Text text) {
        renderTextAboveEntity(matrices, vertexConsumers, entity, text, 0.025F);
    }

    public void renderTextAboveEntity(@NotNull MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers,
                                      @NotNull Entity entity,
                                      Text text,
                                      float scale) {
        // save the current matrix state
        matrices.push();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        matrices.translate(entity.getX() - camX, entity.getY() - camY + 1.35, entity.getZ() - camZ);

        // make the text face the camera
        matrices.multiply(camera.getRotation());
        matrices.multiply(POSITIVE_Y.rotationDegrees(180.0F));

        // scale the text down so it's not too big
        matrices.scale(-scale, -scale, scale);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // calculate the width of the text to center it
        float textWidth = -textRenderer.getWidth(text) / 2.0F;

        // render the text
        textRenderer.draw(text, textWidth, 0.0F, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x55000000, 0xF000F0);

        // restore the previous matrix state
        matrices.pop();
    }

    public void drawLine(@NotNull VertexConsumer consumer,
                         Matrix4f matrix,
                         float x1,
                         float y1,
                         float z1,
                         float x2,
                         float y2,
                         float z2) {
        consumer.vertex(matrix, x1, y1, z1).color(1f, 1f, 0f, 0.6f).normal(0, 1, 0);
        consumer.vertex(matrix, x2, y2, z2).color(1f, 1f, 0f, 0.6f).normal(0, 1, 0);
    }

    public Color getSecondaryColor(@NotNull Color color) {
        return new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2, 100);
    }

    public void initializeWidgets() {
        this.widgets = stream(getAnnotated(PKUtilsWidget.class).spliterator(), false)
                .map(pkUtilsWidgetClass -> {
                    try {
                        return (AbstractPKUtilsWidget<?>) pkUtilsWidgetClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .peek(AbstractPKUtilsWidget::init)
                .collect(toSet());
    }
}
