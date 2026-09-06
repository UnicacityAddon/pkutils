package de.rettichlp.ucutils.common.gui.screens.options;

import de.rettichlp.ucutils.common.configuration.options.MiscellaneousOptions;
import de.rettichlp.ucutils.common.gui.screens.OptionsScreen;
import de.rettichlp.ucutils.common.gui.screens.components.CyclingButtonEntry;
import de.rettichlp.ucutils.common.gui.screens.components.ToggleButtonWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.client.gui.components.Tooltip.create;
import static net.minecraft.network.chat.CommonComponents.NEW_LINE;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.util.CommonColors.RED;

public class MiscellaneousOptionsScreen extends OptionsScreen {

    private static final Component TEXT_MISCELLANEOUS = translatable("ucutils.options.text.miscellaneous");
    private static final Component HYDRATION_NAME = translatable("ucutils.options.hydration.name");
    private static final Component HYDRATION_TOOLTIP = translatable("ucutils.options.hydration.tooltip");
    private static final Component HYDRATION_TEXTURE_NAME = translatable("ucutils.options.hydration_texture.name");
    private static final Component BANK_INFORMATION_NAME = translatable("ucutils.options.atm_information.name");
    private static final Component AUTO_TRASH_CAN_NAME = translatable("ucutils.options.auto_trash_can.name");
    private static final Component AUTO_TRASH_CAN_TOOLTIP = translatable("ucutils.options.auto_trash_can.tooltip");
    private static final Component HIGHLIGHT_CORPSES_NAME = translatable("ucutils.options.highlight_corpses.name");
    private static final Component HIGHLIGHT_CORPSES_TOOLTIP = translatable("ucutils.options.highlight_corpses.tooltip").append(NEW_LINE).append(translatable("ucutils.feature_disabled").withColor(RED));
    private static final Component HIDE_DOLPHINS_NAME = translatable("ucutils.options.hide_dolphins.name");
    private static final Component HIDE_DOLPHINS_TOOLTIP = translatable("ucutils.options.hide_dolphins.tooltip");
    private static final Component BLOCK_MALLE_SOUND_NAME = translatable("ucutils.options.block_malle_sound.name");
    private static final Component BLOCK_MALLE_SOUND_TOOLTIP = translatable("ucutils.options.block_malle_sound.tooltip");
    private static final Component AUTO_SIREN_NAME = translatable("ucutils.options.auto_siren.name");
    private static final Component AUTO_SIREN_TOOLTIP = translatable("ucutils.options.auto_siren.tooltip");

    public MiscellaneousOptionsScreen(Screen parent) {
        super(parent, TEXT_MISCELLANEOUS);
    }

    @Override
    public void initBody() {
        GridLayout gridLayout = this.layout.addToContents(new GridLayout());
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper gridLayoutRowHelper = gridLayout.createRowHelper(2);

        MiscellaneousOptions miscellaneousOptions = configuration.getOptions().miscellaneous();

        ToggleButtonWidget toggleButton1 = new ToggleButtonWidget(HYDRATION_NAME, miscellaneousOptions::showHydration, miscellaneousOptions.showHydration());
        toggleButton1.setWidth(150);
        toggleButton1.setTooltip(create(HYDRATION_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton1);

        CycleButton<MiscellaneousOptions.HydrationTextureType> cycleButton1 = CycleButton.builder(MiscellaneousOptions.HydrationTextureType::getDisplayName, miscellaneousOptions.hydrationTextureType())
                .withValues(MiscellaneousOptions.HydrationTextureType.values())
                .withTooltip(CyclingButtonEntry::getTooltip)
                .create(HYDRATION_TEXTURE_NAME, (_, value) -> miscellaneousOptions.hydrationTextureType(value));
        cycleButton1.setWidth(150);
        gridLayoutRowHelper.addChild(cycleButton1);

        CycleButton<MiscellaneousOptions.AtmInformationType> cycleButton2 = CycleButton.builder(MiscellaneousOptions.AtmInformationType::getDisplayName, miscellaneousOptions.atmInformationType())
                .withValues(MiscellaneousOptions.AtmInformationType.values())
                .withTooltip(CyclingButtonEntry::getTooltip)
                .create(BANK_INFORMATION_NAME, (_, value) -> miscellaneousOptions.atmInformationType(value));
        cycleButton2.setWidth(150);
        gridLayoutRowHelper.addChild(cycleButton2);

        Button button1 = Button.builder(AUTO_TRASH_CAN_NAME, _ -> this.minecraft.gui.setScreen(new TrashCanOptionsScreen(this)))
                .tooltip(create(AUTO_TRASH_CAN_TOOLTIP))
                .width(150)
                .build();
        gridLayoutRowHelper.addChild(button1);

        ToggleButtonWidget toggleButton2 = new ToggleButtonWidget(HIGHLIGHT_CORPSES_NAME, miscellaneousOptions::highlightCorpses, miscellaneousOptions.highlightCorpses());
        toggleButton2.setWidth(150);
        toggleButton2.setTooltip(create(HIGHLIGHT_CORPSES_TOOLTIP));
        toggleButton2.active = commandService.isSuperUser();
        gridLayoutRowHelper.addChild(toggleButton2);

        ToggleButtonWidget toggleButton3 = new ToggleButtonWidget(HIDE_DOLPHINS_NAME, miscellaneousOptions::hideDolphins, miscellaneousOptions.hideDolphins());
        toggleButton3.setWidth(150);
        toggleButton3.setTooltip(create(HIDE_DOLPHINS_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton3);

        ToggleButtonWidget toggleButton4 = new ToggleButtonWidget(BLOCK_MALLE_SOUND_NAME, miscellaneousOptions::blockMalleSound, miscellaneousOptions.blockMalleSound());
        toggleButton4.setWidth(150);
        toggleButton4.setTooltip(create(BLOCK_MALLE_SOUND_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton4);

        ToggleButtonWidget toggleButton5 = new ToggleButtonWidget(AUTO_SIREN_NAME, miscellaneousOptions::autoSiren, miscellaneousOptions.autoSiren());
        toggleButton5.setWidth(150);
        toggleButton5.setTooltip(create(AUTO_SIREN_TOOLTIP));
        gridLayoutRowHelper.addChild(toggleButton5);

        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);
    }
}
