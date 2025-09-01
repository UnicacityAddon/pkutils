package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.HudService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static java.util.stream.Collectors.toMap;

@PKUtilsListener
public class HudListener extends PKUtilsBase implements IHudRenderListener {

    private static final int NOTIFICATION_MARGIN = 5;
    private static final int NOTIFICATION_SPACING = 18; // text height (9) + 2 * padding (6) + space between (3)
    private static final int NOTIFICATION_PADDING = 3;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        renderNotifications(drawContext, client, textRenderer);
    }

    private void renderNotifications(DrawContext drawContext, MinecraftClient client, TextRenderer textRenderer) {
        List<HudService.Notification> activeNotifications = hudService.getActiveNotifications();

        if (activeNotifications.isEmpty()) {
            return;
        }

        Map<HudService.Notification, Integer> notificationOffsets = activeNotifications.stream()
                .collect(toMap(notification -> notification, notification -> activeNotifications.indexOf(notification) * NOTIFICATION_SPACING));

        notificationOffsets.forEach((notification, yOffset) -> {
            Text notificationText = notification.getText();
            int textWidth = textRenderer.getWidth(notificationText);
            int textHeight = textRenderer.fontHeight;
            int x = client.getWindow().getScaledWidth() - textWidth - NOTIFICATION_MARGIN;
            int y = NOTIFICATION_MARGIN + yOffset;

            drawContext.fill(
                    x - NOTIFICATION_PADDING,
                    y - NOTIFICATION_PADDING,
                    x + textWidth + NOTIFICATION_PADDING,
                    y + textHeight + NOTIFICATION_PADDING,
                    notification.getBackgroundColor()
            );

            drawContext.drawBorder(
                    x - NOTIFICATION_PADDING,
                    y - NOTIFICATION_PADDING,
                    textWidth + NOTIFICATION_PADDING * 2,
                    textHeight + NOTIFICATION_PADDING * 2,
                    notification.getBorderColor()
            );

            drawContext.drawTextWithShadow(textRenderer, notificationText, x, y, 0xFFFFFF);
        });
    }
}
