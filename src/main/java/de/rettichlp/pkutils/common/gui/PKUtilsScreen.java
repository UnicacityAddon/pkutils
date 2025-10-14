package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.gui.options.components.CyclingButtonEntry;
import de.rettichlp.pkutils.common.gui.options.components.ItemButtonWidget;
import de.rettichlp.pkutils.common.gui.options.components.ToggleButtonWidget;
import de.rettichlp.pkutils.common.models.config.Options;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static java.util.Objects.nonNull;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.item.Items.COMPARATOR;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public abstract class PKUtilsScreen extends Screen {

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    private final Screen parent;

    private Text subTitle = of("v" + getVersion());
    private boolean renderBackground = true;

    public PKUtilsScreen(Text title) {
        super(title);
        this.parent = null;
    }

    public PKUtilsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    public PKUtilsScreen(Text title, Text subTitle, Screen parent) {
        super(title);
        this.parent = parent;
        this.subTitle = subTitle;
    }

    public PKUtilsScreen(Text title, Text subTitle, Screen parent, boolean renderBackground) {
        super(title);
        this.parent = parent;
        this.subTitle = subTitle;
        this.renderBackground = renderBackground;
    }

    public abstract void initBody();

    public abstract void doOnClose();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.renderBackground) {
            drawMenuListBackground(context);
        }

        super.render(context, mouseX, mouseY, delta);

        if (this.renderBackground) {
            drawHeaderAndFooterSeparators(context);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(null);
        doOnClose();
    }

    @Override
    protected void init() {
        initHeader();
        initBody();
        initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    public void back() {
        if (nonNull(this.parent)) {
            this.client.setScreen(this.parent);
            doOnClose();
        } else {
            close();
        }
    }

    public void addButton(@NotNull DirectionalLayoutWidget widget, String key, ButtonWidget.PressAction onPress, int width) {
        ButtonWidget buttonWidget = ButtonWidget.builder(translatable(key), onPress)
                .build();

        buttonWidget.setWidth(width);

        widget.add(buttonWidget);
    }

    public <E extends CyclingButtonEntry> void addCyclingButton(@NotNull DirectionalLayoutWidget widget,
                                                                String key,
                                                                E[] values,
                                                                Function<E, Text> displayNameFunction,
                                                                BiConsumer<Options, E> onValueChange,
                                                                @NotNull Function<Options, E> currentValue,
                                                                int width) {
        MutableText translatable = translatable(key);

        CyclingButtonWidget<E> cyclingButton = CyclingButtonWidget.builder(displayNameFunction)
                .values(values)
                .initially(currentValue.apply(configuration.getOptions()))
                .tooltip(CyclingButtonEntry::getTooltip)
                .build(translatable, (button, value) -> onValueChange.accept(configuration.getOptions(), value));

        cyclingButton.setWidth(width);

        widget.add(cyclingButton);
    }

    public void addToggleButton(@NotNull DirectionalLayoutWidget widget,
                                String key,
                                BiConsumer<Options, Boolean> onPress,
                                @NotNull Function<Options, Boolean> currentValue,
                                int width) {
        Language language = TranslationStorage.getInstance();

        String nameKey = key + ".name";
        if (!language.hasTranslation(nameKey)) {
            throw new IllegalArgumentException("Missing translation for key: " + nameKey);
        }

        String tooltipKey = key + ".description";
        if (!language.hasTranslation(tooltipKey)) {
            throw new IllegalArgumentException("Missing translation for key: " + tooltipKey);
        }

        Text nameText = translatable(nameKey);
        Text tooltipText = translatable(tooltipKey);

        ToggleButtonWidget toggleButton = new ToggleButtonWidget(nameText, value -> {
            LOGGER.debug("Set option '{}' to '{}'", key, value);
            onPress.accept(configuration.getOptions(), value);
        }, currentValue.apply(configuration.getOptions()));

        toggleButton.setWidth(width);
        toggleButton.setTooltip(Tooltip.of(tooltipText));

        widget.add(toggleButton);
    }

    public void addToggleButtonWithSettings(@NotNull DirectionalLayoutWidget widget,
                                            String key,
                                            BiConsumer<Options, Boolean> onPress,
                                            ButtonWidget.PressAction onPressSettings,
                                            @NotNull Function<Options, Boolean> currentValue,
                                            int width) {
        DirectionalLayoutWidget directionalLayoutWidget = widget.add(horizontal());
        addToggleButton(directionalLayoutWidget, key, onPress, currentValue, width - 20);
        addItemButton(directionalLayoutWidget, "pkutils.options.text.options", COMPARATOR, onPressSettings);
    }

    public void addItemButton(@NotNull DirectionalLayoutWidget widget, String key, Item item, ButtonWidget.PressAction onPress) {
        ItemButtonWidget button = new ItemButtonWidget(key, item, onPress);
        widget.add(button);
    }

    protected void initHeader() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addHeader(vertical().spacing(4));
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
        directionalLayoutWidget.add(new TextWidget(this.title, this.textRenderer));
        directionalLayoutWidget.add(new TextWidget(this.subTitle, this.textRenderer));
    }

    protected void initFooter() {}

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

    private String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new NullPointerException("Cannot find version"));
    }
}
