package de.rettichlp.ucutils.common.gui.screens.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static net.minecraft.network.chat.CommonComponents.OPTION_OFF;
import static net.minecraft.network.chat.CommonComponents.OPTION_ON;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.GREEN;
import static net.minecraft.network.chat.TextColor.RED;

public class ToggleButtonWidget extends Button.Plain {

    private final Component text;
    private final Consumer<Boolean> changeListener;
    private boolean state;

    public ToggleButtonWidget(Component text, Consumer<Boolean> changeListener, boolean defaultState) {
        super(0, 0, 150, 20, empty(), button -> {}, DEFAULT_NARRATION);
        this.text = text;
        this.changeListener = changeListener;
        this.state = defaultState;
        updateText();
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);
        this.state = !this.state;
        this.changeListener.accept(this.state);
        setMessage(getText());
    }

    public void updateText() {
        setMessage(getText());
    }

    private Component getText() {
        return this.text.copy()
                .append(literal(":").withColor(GRAY)).append(SPACE)
                .append(this.state ? OPTION_ON.copy().withColor(GREEN) : OPTION_OFF.copy().withColor(RED));
    }
}
