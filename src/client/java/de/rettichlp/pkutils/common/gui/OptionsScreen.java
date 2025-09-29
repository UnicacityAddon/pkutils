package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.gui.components.ItemButtonWidget;
import de.rettichlp.pkutils.common.gui.components.ToggleButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static java.util.Objects.nonNull;
import static net.minecraft.screen.ScreenTexts.DONE;

public abstract class OptionsScreen extends Screen {

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    protected final Screen parent;
    protected DirectionalLayoutWidget body;

    public OptionsScreen(Screen parent) {
        super(Text.translatable("options.title"));
        this.parent = parent;
    }

    public abstract DirectionalLayoutWidget initBody();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawMenuListBackground(context);
        drawHeaderAndFooterSeparators(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        this.layout.addHeader(this.title, this.textRenderer);
        this.layout.addBody(initBody());
        this.layout.addFooter(ButtonWidget.builder(DONE, button -> this.close()).width(200).build());
        this.layout.forEachChild(this::addDrawableChild);
        refreshWidgetPositions();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();

        if (nonNull(this.body)) {
            this.body.refreshPositions();
        }
    }

    public void addButton(@NotNull DirectionalLayoutWidget widget, String key, boolean defaultValue, int width) {
        MutableText translatable = Text.translatable(key);
        ToggleButtonWidget toggleButton = new ToggleButtonWidget(translatable, value -> configService.edit(mainConfig -> {
            LOGGER.debug("Set option '{}' to '{}'", key, value);
            mainConfig.getOptions().put(key, value);
        }), defaultValue);

        toggleButton.setWidth(width);

        widget.add(toggleButton);
    }

    public void addItemButton(@NotNull DirectionalLayoutWidget widget, Item item, ButtonWidget.PressAction onPress) {
        ItemButtonWidget button = new ItemButtonWidget(item, onPress);
        widget.add(button);
    }

    /**
     * @see EntryListWidget#drawHeaderAndFooterSeparators(DrawContext)
     */
    private void drawHeaderAndFooterSeparators(@NotNull DrawContext context) {
        context.drawTexture(RenderLayer::getGuiTextured, INWORLD_HEADER_SEPARATOR_TEXTURE, this.layout.getX(), this.layout.getHeaderHeight(), 0.0F, 0.0F, this.layout.getWidth(), 2, 32, 2);
        context.drawTexture(RenderLayer::getGuiTextured, INWORLD_FOOTER_SEPARATOR_TEXTURE, this.layout.getX(), this.layout.getHeight() - this.layout.getFooterHeight(), 0.0F, 0.0F, this.layout.getWidth(), 2, 32, 2);
    }

    /**
     * @see EntryListWidget#drawMenuListBackground(DrawContext)
     */
    private void drawMenuListBackground(@NotNull DrawContext context) {
        Identifier identifier = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
        context.drawTexture(RenderLayer::getGuiTextured, identifier, this.layout.getX(), this.layout.getHeaderHeight(), 0.0F, 0.0F, this.layout.getWidth(), this.layout.getContentHeight(), 32, 32);
    }
}
