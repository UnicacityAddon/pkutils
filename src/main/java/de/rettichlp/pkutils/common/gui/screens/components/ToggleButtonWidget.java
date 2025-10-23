package de.rettichlp.pkutils.common.gui.screens.components;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static net.minecraft.screen.ScreenTexts.OFF;
import static net.minecraft.screen.ScreenTexts.ON;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;

public class ToggleButtonWidget extends ButtonWidget {

    private final Text text;
    private final Consumer<Boolean> changeListener;
    private boolean state;

    public ToggleButtonWidget(Text text, Consumer<Boolean> changeListener, boolean defaultState) {
        super(0, 0, 150, 20, empty(), button -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.text = text;
        this.changeListener = changeListener;
        this.state = defaultState;
        updateText();
    }

    @Override
    public void onPress() {
        super.onPress();
        this.state = !this.state;
        this.changeListener.accept(this.state);
        setMessage(getText());
    }

    public void updateText() {
        setMessage(getText());
    }

    private Text getText() {
        return this.text.copy()
                .append(of(":").copy().formatted(GRAY)).append(" ")
                .append(this.state ? ON.copy().formatted(GREEN) : OFF.copy().formatted(RED));
    }
}
