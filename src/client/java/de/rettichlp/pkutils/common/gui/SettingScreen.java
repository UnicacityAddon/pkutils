package de.rettichlp.pkutils.common.gui;

import de.rettichlp.pkutils.common.models.SettingSection;
import de.rettichlp.pkutils.common.models.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingScreen extends Screen {

    private TextFieldWidget searchBar;
    private int guiWidth = 500;
    private int guiHeight = 320;
    private int guiX;
    private int guiY;
    private int scrollOffset = 0;
    private int maxScroll;

    private List<SettingSection> filteredSections;
    private SettingSection activeSection = null;

    private final Map<DrugType, TextFieldWidget> drugInputs = new HashMap<>();

    public SettingScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        this.clearChildren();

        this.guiX = (this.width - guiWidth) / 2;
        this.guiY = (this.height - guiHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            this.client.setScreen(null);
        }).dimensions(guiX + guiWidth - 28, guiY + 7, 20, 20).build());

        this.searchBar = new TextFieldWidget(
                this.textRenderer,
                guiX + 8,
                guiY + 35,
                100,
                20,
                Text.literal("Suchen")
        );

        this.searchBar.setMaxLength(50);
        this.searchBar.setEditable(true);
        this.searchBar.setChangedListener(this::updateFilter);
        this.addSelectableChild(this.searchBar);

        this.filteredSections = new ArrayList<>(List.of(SettingSection.values()));
        this.maxScroll = Math.max(0, (filteredSections.size() * 30) - (guiHeight - 60));

        if (activeSection == SettingSection.PERSONAL_USE) {
            setupDrugInputs();
        }
    }

    private void updateFilter(String query) {
        this.filteredSections = List.of(SettingSection.values()).stream()
                .filter(section -> section.getDisplayName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        this.maxScroll = Math.max(0, (filteredSections.size() * 30) - (guiHeight - 60));
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll);
    }

    private void setupDrugInputs() {
        this.drugInputs.clear();

        int startX = guiX + 220;
        int startY = guiY + 60;
        int spacing = 25;

        int y = startY;
        for (DrugType drug : DrugType.values()) {
            TextFieldWidget field = new TextFieldWidget(
                    this.textRenderer,
                    startX,
                    y,
                    60,
                    20,
                    Text.literal(drug.name())
            );
            field.setText("0");
            field.setChangedListener(value -> {
                try {
                    int parsed = Integer.parseInt(value);
                    System.out.println("Gespeichert: " + drug.name() + " = " + parsed);
                } catch (NumberFormatException ignored) {}
            });
            this.addSelectableChild(field);
            this.drugInputs.put(drug, field);

            y += spacing;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double direction) {
        if (mouseX >= guiX && mouseX <= guiX + 110 && mouseY >= guiY + 30 && mouseY <= guiY + guiHeight) {
            if (amount != 0) {
                this.scrollOffset = (int) Math.max(0, Math.min(this.scrollOffset - amount * 10, this.maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount, direction);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.searchBar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        context.fill(guiX, guiY, guiX + guiWidth, guiY + 25, 0xFF303030);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, guiY + 9, 0xFFFFFF);

        int sidebarWidth = 110;
        context.fill(guiX, guiY + 30, guiX + sidebarWidth, guiY + guiHeight, 0xFF252525);
        context.drawVerticalLine(guiX + sidebarWidth, guiY + 30, guiY + guiHeight, Color.DARK_GRAY.getRGB());

        int startY = guiY + 60 - scrollOffset;
        int spacing = 30;

        for (int i = 0; i < filteredSections.size(); i++) {
            final SettingSection section = filteredSections.get(i);
            int y = startY + i * spacing;
            if (y + 20 > guiY + 30 && y < guiY + guiHeight) {
                ButtonWidget button = ButtonWidget.builder(Text.literal(section.getDisplayName()), btn -> {
                    this.activeSection = section;
                    this.init();
                }).dimensions(guiX + 7, y, 95, 20).build();
                button.render(context, mouseX, mouseY, delta);
            }
        }

        if (activeSection == SettingSection.PERSONAL_USE) {
            int startX = guiX + 130;
            int y = guiY + 60;

            for (DrugType drug : DrugType.values()) {
                int iconX = startX + 10;
                int textX = iconX + 20;

                context.drawItem(drug.getItemStack(), iconX, y);
                context.drawText(this.textRenderer, drug.getDisplayName(), textX, y + 5, 0xFFFFFF, false);

                y += spacing;
            }

            this.drugInputs.values().forEach(f -> f.render(context, mouseX, mouseY, delta));
        }

        this.searchBar.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }
}
