package de.rettichlp.pkutils.common.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;

import java.net.URI;

import static de.rettichlp.pkutils.PKUtils.configuration;
import static net.minecraft.client.gui.screen.ConfirmLinkScreen.opening;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.screen.ScreenTexts.BACK;
import static net.minecraft.screen.ScreenTexts.DONE;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public abstract class OptionsScreen extends PKUtilsScreen {

    private static final URI DISCORD_INVITE = URI.create("https://discord.gg/mZGAAwhPHu");
    private static final int DISCORD_COLOR = 0x5865F2;
    private static final URI MODRINTH = URI.create("https://modrinth.com/mod/pkutils");
    private static final int MODRINTH_COLOR = 0x1BD96B;

    public OptionsScreen(Screen parent) {
        super(empty()
                .append("PKUtils").append(" ")
                .append(translatable("options.title")), parent);
    }

    public OptionsScreen(Screen parent, Text subTitle) {
        super(empty()
                .append("PKUtils").append(" ")
                .append(translatable("options.title")), subTitle, parent);
    }

    public OptionsScreen(Screen parent, Text subTitel, boolean renderBackground) {
        super(empty()
                .append("PKUtils").append(" ")
                .append(translatable("options.title")), subTitel, parent, renderBackground);
    }

    @Override
    public void doOnClose() {
        configuration.saveToFile();
    }

    @Override
    protected void initFooter() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(horizontal().spacing(8));
        directionalLayoutWidget.add(ButtonWidget.builder(BACK, button -> back()).width(120).build());
        directionalLayoutWidget.add(ButtonWidget.builder(DONE, button -> close()).width(200).build());
        directionalLayoutWidget.add(ButtonWidget.builder(of("Discord").copy().withColor(DISCORD_COLOR), opening(this, DISCORD_INVITE)).width(56).build());
        directionalLayoutWidget.add(ButtonWidget.builder(of("Modrinth").copy().withColor(MODRINTH_COLOR), opening(this, MODRINTH)).width(56).build());
    }
}
