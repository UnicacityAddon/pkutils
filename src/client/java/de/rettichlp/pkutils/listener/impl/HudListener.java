package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.HudService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;
import java.util.Map;

import static de.rettichlp.pkutils.PKUtilsClient.hudService;
import static java.util.stream.Collectors.toMap;

@PKUtilsListener
public class HudListener extends PKUtilsBase implements IHudRenderListener {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        renderNotifications(drawContext);
    }

    private void renderNotifications(DrawContext drawContext) {
        List<HudService.Notification> activeNotifications = hudService.getActiveNotifications();

        if (activeNotifications.isEmpty()) {
            return;
        }

        Map<HudService.Notification, Integer> notificationIndexes = activeNotifications.stream()
                .collect(toMap(notification -> notification, activeNotifications::indexOf));

        notificationIndexes.forEach((notification, notificationIndex) -> renderTextBox(
                drawContext,
                notification.getText(),
                notification.getBackgroundColor(),
                notification.getBorderColor(),
                notificationIndex * TEXT_BOX_FULL_SIZE_Y));
    }
}
