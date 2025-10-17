package de.rettichlp.pkutils.common.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;

import java.time.Instant;

import static java.time.Instant.now;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;

@Data
@RequiredArgsConstructor
public class TodoEntry {

    private final String task;
    private boolean done = false;
    private Instant createdAt = now();

    public MutableText getDoneButton() {
        return of("✔").copy().styled(style -> style
                .withColor(GREEN)
                .withClickEvent(new ClickEvent(RUN_COMMAND, "/todo done " + this.createdAt))
                .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Als erledigt markieren"))));
    }

    public MutableText getDeleteButton() {
        return of("✖").copy().styled(style -> style
                .withColor(RED)
                .withClickEvent(new ClickEvent(RUN_COMMAND, "/todo delete " + this.createdAt))
                .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Löschen"))));
    }
}
