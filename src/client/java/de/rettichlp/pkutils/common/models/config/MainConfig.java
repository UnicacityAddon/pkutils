package de.rettichlp.pkutils.common.models.config;

import de.rettichlp.pkutils.common.models.Job;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MainConfig {

    private final Map<Job, LocalDateTime> jobCooldownEndTimes = new HashMap<>();
    private List<TodoEntry> todos = new ArrayList<>();
    private Options options = new Options();
    private int minutesSinceLastPayDay = 0;
    private int predictedPayDaySalary = 0;

    public void addMinutesSinceLastPayDay(int minutes) {
        this.minutesSinceLastPayDay += minutes;
    }

    public void addPredictedPayDaySalary(int salary) {
        this.predictedPayDaySalary += salary;
    }
}
