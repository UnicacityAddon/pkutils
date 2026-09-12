package de.rettichlp.ucutils.common.gui.screens.options;

import de.rettichlp.ucutils.common.gui.screens.OptionsScreen;
import de.rettichlp.ucutils.common.gui.screens.components.ToggleButtonWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.world.item.Items.APPLE;
import static net.minecraft.world.item.Items.CARROT;
import static net.minecraft.world.item.Items.CHEST;
import static net.minecraft.world.item.Items.ENDER_CHEST;
import static net.minecraft.world.item.Items.PAPER;
import static net.minecraft.world.item.Items.POTATO;
import static net.minecraft.world.item.Items.ROTTEN_FLESH;
import static net.minecraft.world.item.Items.TRAPPED_CHEST;

public class TrashCanOptionsScreen extends OptionsScreen {

    public static final Component TEXT_TRASH_CAN = translatable("ucutils.options.auto_trash_can.name");

    private static final Set<String> CHEST_ITEMS = Set.of(CHEST.toString(), TRAPPED_CHEST.toString(), ENDER_CHEST.toString());

    public TrashCanOptionsScreen(Screen parent) {
        super(parent, TEXT_TRASH_CAN);
    }

    @Override
    public void initBody() {
        GridLayout gridLayout = this.layout.addToContents(new GridLayout());
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper gridLayoutRowHelper = gridLayout.createRowHelper(2);

        Set<String> autoCollectFromTrashCan = configuration.getOptions().autoCollectFromTrashCan();

        addToggleButton(gridLayoutRowHelper, translatable("block.minecraft.chest"), autoCollectFromTrashCan.containsAll(CHEST_ITEMS), value -> {
            if (value) {
                autoCollectFromTrashCan.addAll(CHEST_ITEMS);
            } else {
                autoCollectFromTrashCan.removeAll(CHEST_ITEMS);
            }
        });
        addToggleButton(gridLayoutRowHelper, translatable("item.minecraft.apple"), autoCollectFromTrashCan.contains(APPLE.toString()), value -> setItemState(autoCollectFromTrashCan, APPLE.toString(), value));
        addToggleButton(gridLayoutRowHelper, translatable("item.minecraft.carrot"), autoCollectFromTrashCan.contains(CARROT.toString()), value -> setItemState(autoCollectFromTrashCan, CARROT.toString(), value));
        addToggleButton(gridLayoutRowHelper, translatable("item.minecraft.paper"), autoCollectFromTrashCan.contains(PAPER.toString()), value -> setItemState(autoCollectFromTrashCan, PAPER.toString(), value));
        addToggleButton(gridLayoutRowHelper, translatable("item.minecraft.potato"), autoCollectFromTrashCan.contains(POTATO.toString()), value -> setItemState(autoCollectFromTrashCan, POTATO.toString(), value));
        addToggleButton(gridLayoutRowHelper, translatable("item.minecraft.rotten_flesh"), autoCollectFromTrashCan.contains(ROTTEN_FLESH.toString()), value -> setItemState(autoCollectFromTrashCan, ROTTEN_FLESH.toString(), value));

        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);
    }

    private void addToggleButton(GridLayout.@NonNull RowHelper gridLayoutRowHelper,
                                 Component name,
                                 boolean currentValue,
                                 Consumer<Boolean> setter) {
        ToggleButtonWidget toggleButton = new ToggleButtonWidget(name, setter, currentValue);
        toggleButton.setWidth(150);
        gridLayoutRowHelper.addChild(toggleButton);
    }

    private void setItemState(Collection<String> autoCollectFromTrashCan, String item, boolean value) {
        if (value) {
            autoCollectFromTrashCan.add(item);
        } else {
            autoCollectFromTrashCan.remove(item);
        }
    }
}
