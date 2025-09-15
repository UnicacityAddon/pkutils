package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.models.SettingSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingScreen extends Screen {

    private TextFieldWidget searchBar;
    private int guiWidth = 400;
    private int guiHeight = 300;
    private int guiX;
    private int guiY;
    private int scrollOffset = 0;
    private int maxScroll;
    private List<SettingSection> filteredSections;

    public SettingScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        this.guiX = (this.width - guiWidth) / 2;
        this.guiY = (this.height - guiHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            this.client.setScreen(null);
        }).dimensions(guiX + guiWidth - 25, guiY + 5, 20, 20).build());

        this.searchBar = new TextFieldWidget(
                this.textRenderer,
                guiX + 8,
                guiY + 35,
                guiWidth - 300,
                20,
                Text.literal("Suchen")
        );
        this.searchBar.setMaxLength(50);
        this.searchBar.setEditable(true);
        this.searchBar.setChangedListener(this::updateFilter);
        this.addSelectableChild(this.searchBar);

        this.filteredSections = new ArrayList<>(List.of(SettingSection.values()));
        this.maxScroll = Math.max(0, (filteredSections.size() * 30) - (guiHeight - 60));
    }

    private void updateFilter(String query) {
        this.filteredSections = List.of(SettingSection.values()).stream()
                .filter(section -> section.getDisplayName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        this.maxScroll = Math.max(0, (filteredSections.size() * 30) - (guiHeight - 60));
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll);
        this.init();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double direction) {
        if (amount != 0) {
            this.scrollOffset = (int) Math.max(0, Math.min(this.scrollOffset - amount * 10, this.maxScroll));
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        MinecraftClient client = MinecraftClient.getInstance();
        client.options.getMenuBackgroundBlurriness().setValue(0);

        context.fill(guiX, guiY, guiX + guiWidth, guiY + guiHeight, 0xFF202020);
        context.fill(guiX, guiY, guiX + guiWidth, guiY + 25, 0xFF303030);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, guiY + 9, 0xFFFFFF);

        int sidebarWidth = 110;
        context.fill(guiX, guiY + 25, guiX + sidebarWidth, guiY + guiHeight, 0xFF252525);
        context.drawVerticalLine(guiX + sidebarWidth, guiY + 25, guiY + guiHeight, Color.DARK_GRAY.getRGB());

        int startY = guiY + 60 - scrollOffset;
        int spacing = 30;
        for (int i = 0; i < filteredSections.size(); i++) {
            final int index = i;
            int y = startY + index * spacing;
            if (y + 20 > guiY + 25 && y < guiY + guiHeight) {
                ButtonWidget button = ButtonWidget.builder(Text.literal(filteredSections.get(index).getDisplayName()), btn -> {
                    System.out.println("Button " + filteredSections.get(index).name() + " wurde angeklickt!");
                }).dimensions(guiX + 7, y, 100, 20).build();
                this.addDrawableChild(button);
            }
        }

        this.searchBar.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}