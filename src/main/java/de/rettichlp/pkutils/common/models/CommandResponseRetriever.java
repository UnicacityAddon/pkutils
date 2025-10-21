package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.commandService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@RequiredArgsConstructor
public class CommandResponseRetriever {

    private final List<Matcher> response = new ArrayList<>();
    private final String commandToExecute;
    private final Pattern pattern;
    private final Consumer<List<Matcher>> consumer;

    private long timeoutMillis = 1000;
    private boolean hideMessage = false;
    private LocalDateTime startedAt;

    public CommandResponseRetriever(String commandToExecute, Pattern pattern, Consumer<List<Matcher>> consumer, boolean hideMessage) {
        this.commandToExecute = commandToExecute;
        this.pattern = pattern;
        this.consumer = consumer;
        this.hideMessage = hideMessage;
    }

    public void execute() {
        storage.getCommandResponseRetrievers().add(this);

        if (!commandService.sendCommandWithAfkCheck(this.commandToExecute)) {
            return;
        }

        this.startedAt = now();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                CommandResponseRetriever.this.consumer.accept(CommandResponseRetriever.this.response);
            }
        }, this.timeoutMillis);
    }

    public boolean addAsResultIfMatch(CharSequence message) {
        Matcher matcher = this.pattern.matcher(message);

        if (matcher.find()) {
            this.response.add(matcher);
            return this.hideMessage;
        }

        return false;
    }

    public boolean isActive() {
        return nonNull(this.startedAt) && now().isBefore(this.startedAt.plus(this.timeoutMillis, MILLIS));
    }
}
