package de.rettichlp.pkutils.common.gui.screens.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.ASCENDING;
import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.DESCENDING;
import static de.rettichlp.pkutils.common.gui.screens.components.TableHeaderTextWidget.SortingDirection.NONE;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.AQUA;

public class TableHeaderTextWidget extends ClickableWidget {

    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;

    private final Text text;
    private final Consumer<SortingDirection> onChange;
    private SortingDirection sortingDirection;

    public TableHeaderTextWidget(Text text, Consumer<SortingDirection> onChange, SortingDirection initialSortingDirection) {
        super(0, 0, TEXT_RENDERER.getWidth(text), TEXT_RENDERER.fontHeight, empty());
        this.text = text;
        this.onChange = onChange;
        this.sortingDirection = initialSortingDirection;
    }

    @Override
    protected void renderWidget(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        Text modifiedText = getModifiedText();
        int textWidth = TEXT_RENDERER.getWidth(modifiedText);
        context.drawText(TEXT_RENDERER, modifiedText, getX() + (getWidth() - textWidth) / 2, getY(), 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);

        if (button != 0 || !isMouseOver(mouseX, mouseY)) {
            return mouseClicked;
        }

        this.sortingDirection = switch (this.sortingDirection) {
            case NONE -> ASCENDING;
            case ASCENDING -> DESCENDING;
            case DESCENDING -> NONE;
        };

        this.onChange.accept(this.sortingDirection);

        return mouseClicked;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    private Text getModifiedText() {
        String arrow = switch (this.sortingDirection) {
            case ASCENDING -> " ↓";
            case DESCENDING -> " ↑";
            default -> "";
        };

        return this.text.copy().append(of(arrow).copy().formatted(AQUA));
    }

    public enum SortingDirection {

        NONE,
        ASCENDING,
        DESCENDING;

        public <T> Comparator<T> apply(Comparator<T> comparator) {
            return this == DESCENDING ? comparator.reversed() : comparator;
        }
    }
}
