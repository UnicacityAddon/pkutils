package de.rettichlp.pkutils.common.gui.screens;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static java.util.Objects.nonNull;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.of;

public abstract class PKUtilsScreen extends Screen {

    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    private final Screen parent;

    private Text subTitle = of("v" + getVersion());
    private boolean renderBackground = true;

    public PKUtilsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    public PKUtilsScreen(Text title, Text subTitle) {
        super(title);
        this.parent = null;
        this.subTitle = subTitle;
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
