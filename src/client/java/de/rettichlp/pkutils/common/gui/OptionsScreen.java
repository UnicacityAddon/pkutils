package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.gui.components.ItemButtonWidget;
import de.rettichlp.pkutils.common.gui.components.ToggleButtonWidget;
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

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static net.minecraft.client.gui.screen.ConfirmLinkScreen.opening;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.item.Items.COMPARATOR;
import static net.minecraft.screen.ScreenTexts.BACK;
import static net.minecraft.screen.ScreenTexts.DONE;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public abstract class OptionsScreen extends Screen {

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    private static final URI DISCORD_INVITE = URI.create("https://discord.gg/mZGAAwhPHu");
    private static final int DISCORD_COLOR = 0x5865F2;
    private static final URI MODRINTH = URI.create("https://modrinth.com/mod/pkutils");
    private static final int MODRINTH_COLOR = 0x1BD96B;

    private final Screen parent;
    private final Text subTitle;

    public OptionsScreen(Screen parent) {
        super(empty()
                .append("PKUtils").append(" ")
                .append(translatable("options.title")));
        this.parent = parent;
        this.subTitle = of("v" + getVersion());
    }

    public OptionsScreen(Screen parent, String subTitelKey) {
        super(empty()
                .append("PKUtils").append(" ")
                .append(translatable("options.title")));
        this.parent = parent;
        this.subTitle = translatable(subTitelKey);
    }

    public abstract void initBody();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawMenuListBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawHeaderAndFooterSeparators(context);
    }

    @Override
    public void close() {
        this.client.setScreen(null);
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
        this.client.setScreen(this.parent);
    }

    public void addButton(@NotNull DirectionalLayoutWidget widget, String key, ButtonWidget.PressAction onPress, int width) {
        ButtonWidget buttonWidget = ButtonWidget.builder(translatable(key), onPress)
                .build();

        buttonWidget.setWidth(width);

        widget.add(buttonWidget);
    }

    public <E> void addCyclingButton(@NotNull DirectionalLayoutWidget widget,
                                     String key,
                                     E[] values,
                                     Function<E, Text> displayNameFunction,
                                     BiConsumer<Options, E> onValueChange,
                                     @NotNull Function<Options, E> currentValue,
                                     int width) {
        MutableText translatable = translatable(key);

        CyclingButtonWidget<E> cyclingButton = CyclingButtonWidget.builder(displayNameFunction)
                .values(values)
                .initially(currentValue.apply(configService.load().getOptions()))
                .build(translatable, (button, value) -> configService.edit(mainConfig -> onValueChange.accept(mainConfig.getOptions(), value)));

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

        ToggleButtonWidget toggleButton = new ToggleButtonWidget(nameText, value -> configService.edit(mainConfig -> {
            LOGGER.debug("Set option '{}' to '{}'", key, value);
            onPress.accept(mainConfig.getOptions(), value);
        }), currentValue.apply(configService.load().getOptions()));

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

    private void initHeader() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addHeader(vertical().spacing(4));
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
        directionalLayoutWidget.add(new TextWidget(this.title, this.textRenderer));
        directionalLayoutWidget.add(new TextWidget(this.subTitle, this.textRenderer));
    }

    private void initFooter() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(horizontal().spacing(8));
        directionalLayoutWidget.add(ButtonWidget.builder(BACK, button -> back()).width(120).build());
        directionalLayoutWidget.add(ButtonWidget.builder(DONE, button -> close()).width(200).build());
        directionalLayoutWidget.add(ButtonWidget.builder(of("Discord").copy().withColor(DISCORD_COLOR), opening(this, DISCORD_INVITE)).width(56).build());
        directionalLayoutWidget.add(ButtonWidget.builder(of("Modrinth").copy().withColor(MODRINTH_COLOR), opening(this, MODRINTH)).width(56).build());
    }

    private String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new NullPointerException("Cannot find version"));
    }
}
