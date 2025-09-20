package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.Color;

import static net.minecraft.client.render.RenderLayer.getLines;
import static net.minecraft.util.math.RotationAxis.POSITIVE_Y;

public class RenderService extends PKUtilsBase {

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

    public void renderTextBox(@NotNull DrawContext drawContext,
                              Text text,
                              @NotNull Color backgroundColor,
                              @NotNull Color borderColor,
                              int boxIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;
        int x = client.getWindow().getScaledWidth() - textWidth - TEXT_BOX_MARGIN;
        int y = TEXT_BOX_FULL_SIZE_Y * boxIndex + TEXT_BOX_MARGIN;

        drawContext.fill(
                x - TEXT_BOX_PADDING,
                y - TEXT_BOX_PADDING,
                x + textWidth + TEXT_BOX_PADDING,
                y + textHeight + TEXT_BOX_PADDING,
                backgroundColor.getRGB()
        );

        drawContext.drawBorder(
                x - TEXT_BOX_PADDING,
                y - TEXT_BOX_PADDING,
                textWidth + TEXT_BOX_PADDING * 2,
                textHeight + TEXT_BOX_PADDING * 2,
                borderColor.getRGB()
        );

        drawContext.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
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
}
