package de.rettichlp.ucutils.common.gui.screens.options;

import de.rettichlp.ucutils.common.configuration.options.NotificationOptions;
import de.rettichlp.ucutils.common.gui.screens.OptionsScreen;
import de.rettichlp.ucutils.common.gui.screens.components.ToggleButtonWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.network.chat.Component.translatable;

public class NotificationOptionsScreen extends OptionsScreen {

    private static final Component TEXT_NOTIFICATIONS = translatable("ucutils.options.text.notifications");
    private static final Component NOTIFICATION_JOIN_QUIT_NAME = translatable("ucutils.options.notifications.join_quit.name");
    private static final Component NOTIFICATION_JOIN_QUIT_TOOLTIP = translatable("ucutils.options.notifications.join_quit.tooltip");
    private static final Component NOTIFICATION_ADUTY_NAME = translatable("ucutils.options.notifications.aduty.name");
    private static final Component NOTIFICATION_ADUTY_TOOLTIP = translatable("ucutils.options.notifications.aduty.tooltip");
    private static final Component NOTIFICATION_REPORT_NAME = translatable("ucutils.options.notifications.report.name");
    private static final Component NOTIFICATION_REPORT_TOOLTIP = translatable("ucutils.options.notifications.report.tooltip");
    private static final Component NOTIFICATION_BUILD_MODE_NAME = translatable("ucutils.options.notifications.build_mode.name");
    private static final Component NOTIFICATION_BUILD_MODE_TOOLTIP = translatable("ucutils.options.notifications.build_mode.tooltip");
    private static final Component NOTIFICATION_JOB_COOLDOWN_NAME = translatable("ucutils.options.notifications.job_cooldown.name");
    private static final Component NOTIFICATION_JOB_COOLDOWN_TOOLTIP = translatable("ucutils.options.notifications.job_cooldown.tooltip");

    public NotificationOptionsScreen(Screen parent) {
        super(parent, TEXT_NOTIFICATIONS);
    }

    @Override
    public void initBody() {
        GridLayout gridLayout = this.layout.addToContents(new GridLayout());
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper gridLayoutRowHelper = gridLayout.createRowHelper(2);

        NotificationOptions notificationOptions = configuration.getOptions().notification();

        ToggleButtonWidget toggleButton1 = new ToggleButtonWidget(NOTIFICATION_JOIN_QUIT_NAME, notificationOptions::joinQuit, notificationOptions.joinQuit());
        toggleButton1.setWidth(150);
        toggleButton1.setTooltip(Tooltip.create(NOTIFICATION_JOIN_QUIT_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton1);

        ToggleButtonWidget toggleButton2 = new ToggleButtonWidget(NOTIFICATION_ADUTY_NAME, notificationOptions::aDuty, notificationOptions.aDuty());
        toggleButton2.setWidth(150);
        toggleButton2.setTooltip(Tooltip.create(NOTIFICATION_ADUTY_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton2);

        ToggleButtonWidget toggleButton3 = new ToggleButtonWidget(NOTIFICATION_REPORT_NAME, notificationOptions::report, notificationOptions.report());
        toggleButton3.setWidth(150);
        toggleButton3.setTooltip(Tooltip.create(NOTIFICATION_REPORT_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton3);

        ToggleButtonWidget toggleButton4 = new ToggleButtonWidget(NOTIFICATION_BUILD_MODE_NAME, notificationOptions::buildMode, notificationOptions.buildMode());
        toggleButton4.setWidth(150);
        toggleButton4.setTooltip(Tooltip.create(NOTIFICATION_BUILD_MODE_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton4);

        ToggleButtonWidget toggleButton5 = new ToggleButtonWidget(NOTIFICATION_JOB_COOLDOWN_NAME, notificationOptions::jobCooldown, notificationOptions.jobCooldown());
        toggleButton5.setWidth(150);
        toggleButton5.setTooltip(Tooltip.create(NOTIFICATION_JOB_COOLDOWN_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton5);

        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);
    }
}
