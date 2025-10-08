package de.rettichlp.pkutils.common.models.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainConfig {

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
