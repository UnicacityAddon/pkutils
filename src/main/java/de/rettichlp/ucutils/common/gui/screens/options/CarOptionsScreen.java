package de.rettichlp.ucutils.common.gui.screens.options;

import de.rettichlp.ucutils.common.configuration.options.CarOptions;
import de.rettichlp.ucutils.common.gui.screens.OptionsScreen;
import de.rettichlp.ucutils.common.gui.screens.components.ToggleButtonWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.network.chat.TextColor.GOLD;

public class CarOptionsScreen extends OptionsScreen {

    private static final Component TEXT_CAR = translatable("ucutils.options.text.car");
    private static final Component CAR_PREMIUM_INFO = translatable("ucutils.options.car.premium_info");
    private static final Component CAR_GENERAL_FAST_FIND_NAME = translatable("ucutils.options.car.general.fast_find.name");
    private static final Component CAR_GENERAL_FAST_FIND_TOOLTIP = translatable("ucutils.options.car.general.fast_find.tooltip");
    private static final Component CAR_GENERAL_FAST_LOCK_NAME = translatable("ucutils.options.car.general.fast_lock.name");
    private static final Component CAR_GENERAL_FAST_LOCK_TOOLTIP = translatable("ucutils.options.car.general.fast_lock.tooltip");
    private static final Component CAR_GENERAL_HIGHLIGHT_NAME = translatable("ucutils.options.car.general.highlight.name");
    private static final Component CAR_GENERAL_HIGHLIGHT_TOOLTIP = translatable("ucutils.options.car.general.highlight.tooltip");
    private static final Component CAR_AUTOMATION_LOCK_NAME = translatable("ucutils.options.car.automation.lock.name");
    private static final Component CAR_AUTOMATION_LOCK_TOOLTIP = translatable("ucutils.options.car.automation.lock.tooltip");
    private static final Component CAR_AUTOMATION_START_NAME = translatable("ucutils.options.car.automation.start.name");
    private static final Component CAR_AUTOMATION_START_TOOLTIP = translatable("ucutils.options.car.automation.start.tooltip");
    private static final Component CAR_AUTOMATION_CHECK_KFZ_NAME = translatable("ucutils.options.car.automation.check_kfz.name");
    private static final Component CAR_AUTOMATION_CHECK_KFZ_TOOLTIP = translatable("ucutils.options.car.automation.check_kfz.tooltip");

    public CarOptionsScreen(Screen parent) {
        super(parent, TEXT_CAR);
    }

    @Override
    public void initBody() {
        GridLayout gridLayout = this.layout.addToContents(new GridLayout());
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper gridLayoutRowHelper = gridLayout.createRowHelper(2);

        CarOptions carOptions = configuration.getOptions().car();

        ToggleButtonWidget toggleButton1 = new ToggleButtonWidget(CAR_GENERAL_HIGHLIGHT_NAME, carOptions::highlight, carOptions.highlight());
        toggleButton1.setWidth(150);
        toggleButton1.setTooltip(Tooltip.create(CAR_GENERAL_HIGHLIGHT_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton1);

        ToggleButtonWidget toggleButton2 = new ToggleButtonWidget(CAR_GENERAL_FAST_FIND_NAME, carOptions::fastFind, carOptions.fastFind());
        toggleButton2.setWidth(150);
        toggleButton2.setTooltip(Tooltip.create(CAR_GENERAL_FAST_FIND_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton2);

        ToggleButtonWidget toggleButton3 = new ToggleButtonWidget(CAR_AUTOMATION_CHECK_KFZ_NAME, carOptions::automatedCheckKfz, carOptions.automatedCheckKfz());
        toggleButton3.setWidth(150);
        toggleButton3.setTooltip(Tooltip.create(CAR_AUTOMATION_CHECK_KFZ_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton3);

        MultiLineTextWidget multiLineTextWidget = gridLayoutRowHelper.addChild(new MultiLineTextWidget(CAR_PREMIUM_INFO.copy().withColor(GOLD), this.font), 2, LayoutSettings.defaults().alignHorizontallyCenter().paddingTop(16));
        multiLineTextWidget.setMaxWidth(308);
        multiLineTextWidget.setCentered(true);

        ToggleButtonWidget toggleButton4 = new ToggleButtonWidget(CAR_GENERAL_FAST_LOCK_NAME, carOptions::fastLock, carOptions.fastLock());
        toggleButton4.setWidth(150);
        toggleButton4.setTooltip(Tooltip.create(CAR_GENERAL_FAST_LOCK_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton4);

        ToggleButtonWidget toggleButton5 = new ToggleButtonWidget(CAR_AUTOMATION_LOCK_NAME, carOptions::automatedLock, carOptions.automatedLock());
        toggleButton5.setWidth(150);
        toggleButton5.setTooltip(Tooltip.create(CAR_AUTOMATION_LOCK_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton5);

        ToggleButtonWidget toggleButton6 = new ToggleButtonWidget(CAR_AUTOMATION_START_NAME, carOptions::automatedStart, carOptions.automatedStart());
        toggleButton6.setWidth(150);
        toggleButton6.setTooltip(Tooltip.create(CAR_AUTOMATION_START_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton6);

        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);
    }
}
