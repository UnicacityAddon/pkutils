package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.models.SettingSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Arrays;

public class SettingScreen extends Screen {

    public SettingScreen(Text title) {
        super(title);
    }

    private TextFieldWidget searchBar;

    @Override
    public void init() {
        int guiWidth = 400;
        int guiHeight = 300;
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            this.client.setScreen(null);
        }).dimensions(guiX + guiWidth - 25, guiY + 5, 20, 20).build());

        this.searchBar = new TextFieldWidget(
                this.textRenderer,
                guiX + 10,
                guiY + 40,
                guiWidth - 300,
                20,
                Text.literal("Suchen")
        );

        this.addSelectableChild(this.searchBar);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int guiWidth = 400;
        int guiHeight = 300;
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        MinecraftClient client = MinecraftClient.getInstance();
        client.options.getMenuBackgroundBlurriness().setValue(0);

        context.fill(guiX, guiY, guiX + guiWidth, guiY + guiHeight, 0xFF202020);

        context.drawVerticalLine(guiX + 125, guiY + 5, guiY - 5 + guiHeight, Color.DARK_GRAY.getRGB());

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                guiY + 10,
                0xFFFFFF
        );

        Arrays.stream(SettingSection.values())
                .forEach(section -> {
                    int index = section.ordinal() - 1;
                    int buttonY = guiY + 70 + (index * 30);

                    context.drawTextWithShadow(
                            this.textRenderer,
                            section.getDisplayName(),
                            guiX + 10,
                            buttonY + 6,
                            0xFFFFFF
                    );

                    context.drawHorizontalLine(guiX + 10, guiX + 115, buttonY + 20, Color.DARK_GRAY.getRGB());
                });


        this.searchBar.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }
}