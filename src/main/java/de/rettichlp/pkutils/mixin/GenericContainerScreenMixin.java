package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.common.gui.options.components.ToggleButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.PKUtils.utilService;
import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

    public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, @Nullable Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onContainerInit(CallbackInfo ci) {
        if (!isWhitelistedInventory()) {
            return;
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Window window = MinecraftClient.getInstance().getWindow();

        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();

        int widgetHeight = 20;

        int y = (scaledHeight - this.backgroundHeight) / 2 - widgetHeight - 4;
        int buttonX = (scaledWidth - this.backgroundWidth) / 2;
        int buttonWidth = this.backgroundWidth / 2; // 3/6
        int textX = buttonX + buttonWidth;
        int textWidth = this.backgroundWidth / 3; // 2/6
        int textFieldX = textX + textWidth;
        int textFieldWith = this.backgroundWidth / 6; // 1/6

        ToggleButtonWidget toggleButton = new ToggleButtonWidget(of("ABuy"), storage::setABuyEnabled, storage.isABuyEnabled());
        toggleButton.setX(buttonX);
        toggleButton.setY(y);
        toggleButton.setWidth(buttonWidth);
        addDrawableChild(toggleButton);

        TextWidget textWidget = new TextWidget(textX, y, textWidth, 20, of("Anzahl:"), textRenderer);
        addDrawableChild(textWidget);

        TextFieldWidget textField = new TextFieldWidget(textRenderer, textFieldX, y, textFieldWith, widgetHeight, empty());
        textField.setMaxLength(2);
        textField.setPlaceholder(of(valueOf(storage.getABuyAmount())));
        textField.setChangedListener(this::onTextFieldChange);
        addDrawableChild(textField);
    }

    @Unique
    private boolean isWhitelistedInventory() {
        if (isNull(this.title)) {
            return false;
        }

        String title = this.title.getString();
        return utilService.getWhitelistedInventoryTitles().stream().anyMatch(title::contains);
    }

    @Unique
    private void onTextFieldChange(String input) {
        try {
            int parsedInt = parseInt(input);
            storage.setABuyAmount(max(1, parsedInt));
        } catch (NumberFormatException e) {
            storage.setABuyAmount(10);
        }
    }
}
