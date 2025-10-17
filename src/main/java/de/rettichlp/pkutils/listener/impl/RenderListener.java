package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.common.gui.widgets.alignment.AlignVerticalWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;
import de.rettichlp.pkutils.common.gui.widgets.base.PKUtilsWidget;
import de.rettichlp.pkutils.common.models.Countdown;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.services.NotificationService;
import de.rettichlp.pkutils.listener.IHudRenderListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.Objects;

import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.RIGHT;
import static java.util.stream.StreamSupport.stream;
import static org.atteo.classindex.ClassIndex.getAnnotated;

@PKUtilsListener
public class RenderListener extends PKUtilsBase implements IHudRenderListener {

    private final AlignVerticalWidget notificationOverlay = new AlignVerticalWidget();

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        renderNotifications(drawContext);
        renderWidgets(drawContext);
    }

    private void renderNotifications(DrawContext drawContext) {
        this.notificationOverlay.clear();

        storage.getCountdowns().stream()
                .filter(Countdown::isActive)
                .map(Countdown::toWidget)
                .forEach(this.notificationOverlay::add);

        notificationService.getActiveNotifications().stream()
                .map(NotificationService.Notification::toWidget)
                .forEach(this.notificationOverlay::add);

        this.notificationOverlay.draw(drawContext, MinecraftClient.getInstance().getWindow().getScaledWidth() - this.notificationOverlay.getWidth(), 0, RIGHT);
    }

    private void renderWidgets(DrawContext drawContext) {
        stream(getAnnotated(PKUtilsWidget.class).spliterator(), false)
                .map(pkUtilsWidgetClass -> {
                    try {
                        return (AbstractPKUtilsWidget<?>) pkUtilsWidgetClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(pkUtilsWidgetInstance -> pkUtilsWidgetInstance.draw(drawContext));
    }
}
