package de.rettichlp.ucutils.common.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;

import static de.rettichlp.ucutils.UCUtils.MOD_NAME;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static net.minecraft.client.gui.layouts.LinearLayout.horizontal;
import static net.minecraft.client.gui.screens.ConfirmLinkScreen.confirmLink;
import static net.minecraft.network.chat.CommonComponents.GUI_BACK;
import static net.minecraft.network.chat.CommonComponents.GUI_DONE;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public abstract class OptionsScreen extends UCUtilsScreen {

    private static final URI DISCORD_INVITE = URI.create("https://discord.gg/mZGAAwhPHu");
    private static final int DISCORD_COLOR = 0x5865F2;
    private static final URI MODRINTH = URI.create("https://modrinth.com/mod/ucutils");
    private static final int MODRINTH_COLOR = 0x1BD96B;

    public OptionsScreen(Screen parent) {
        super(empty()
                .append(MOD_NAME).append(SPACE)
                .append(translatable("options.title")), parent);
    }

    public OptionsScreen(Screen parent, Component subTitle) {
        super(empty()
                .append(MOD_NAME).append(SPACE)
                .append(translatable("options.title")), subTitle, parent);
    }

    public OptionsScreen(Screen parent, Component subTitel, boolean renderBackground) {
        super(empty()
                .append(MOD_NAME).append(SPACE)
                .append(translatable("options.title")), subTitel, parent, renderBackground);
    }

    @Override
    public void doOnClose() {
        configuration.saveToFile();
    }

    @Override
    protected void initFooter() {
        LinearLayout directionalLayoutWidget = this.layout.addToFooter(horizontal().spacing(8));
        directionalLayoutWidget.addChild(Button.builder(GUI_BACK, _ -> back()).width(120).build());
        directionalLayoutWidget.addChild(Button.builder(GUI_DONE, _ -> onClose()).width(200).build());
        directionalLayoutWidget.addChild(Button.builder(literal("Discord").withColor(DISCORD_COLOR), confirmLink(this, DISCORD_INVITE)).width(56).build());
        directionalLayoutWidget.addChild(Button.builder(literal("Modrinth").withColor(MODRINTH_COLOR), confirmLink(this, MODRINTH)).width(56).build());
    }
}
