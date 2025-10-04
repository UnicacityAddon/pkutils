package de.rettichlp.pkutils.common.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.minecraft.text.Text.translatable;

public class ItemButtonWidget extends ButtonWidget {

    private final Item item;

    public ItemButtonWidget(String key, Item item, PressAction onPress) {
        super(0, 0, 20, 20, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.item = item;
        setTooltip(Tooltip.of(translatable(key)));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        int x = getX() + (getWidth() / 2) - 8;
        int y = getY() + (getHeight() / 2) - 8;

        ItemStack stack = new ItemStack(this.item);
        context.drawItem(stack, x, y);
    }
}
