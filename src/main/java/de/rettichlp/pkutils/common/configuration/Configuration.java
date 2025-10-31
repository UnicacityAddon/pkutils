package de.rettichlp.pkutils.common.configuration;

import de.rettichlp.pkutils.common.configuration.options.Options;
import de.rettichlp.pkutils.common.models.TodoEntry;
import de.rettichlp.pkutils.listener.impl.EventService;
import lombok.Data;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

@Data
public class Configuration {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pkutils.json");

    private Map<String, Object> widgets = new HashMap<>();
    private List<TodoEntry> todos = new ArrayList<>();
    private Options options = new Options();
    private int moneyBankAmount = 0;
    private int moneyCashAmount = 0;
    private int minutesSinceLastPayDay = 0;
    private int predictedPayDaySalary = 0;
    private int predictedPayDayExp = 0;
    @Nullable
    private LocalDateTime firstAidLicenseExpireDateTime = null;
    private int dataUsageConfirmationUID = 0;
    private Set<EventService.HalloweenDoor> halloweenClickedDoors = Set.of();

    public void addMinutesSinceLastPayDay(int minutes) {
        this.minutesSinceLastPayDay += minutes;

        if (this.minutesSinceLastPayDay % 10 == 0) {
            new Thread(this::saveToFile).start(); // save asynchronously every active 10 minutes
        }
    }

    public void addPredictedPayDaySalary(int salary) {
        this.predictedPayDaySalary += salary;
    }

    public void addPredictedPayDayExp(int exp) {
        this.predictedPayDayExp += exp;
    }

    public Configuration loadFromFile() {
        File file = CONFIG_PATH.toFile();

        // create a new config if the file does not exist or is empty
        if (!file.exists() || file.length() == 0) {
            LOGGER.info("Config file does not exist or is empty, creating new one at {}", CONFIG_PATH);
            saveToFile();
            return this;
        }

        // load existing config
        try {
            Reader reader = newBufferedReader(CONFIG_PATH);
            return api.getGson().fromJson(reader, Configuration.class);
        } catch (Exception e) {
            LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
        }

        // fallback
        LOGGER.warn("Failed to load config, using default values");
        saveToFile();

        return this;
    }

    public void saveToFile() {
        try (Writer writer = newBufferedWriter(CONFIG_PATH)) {
            api.getGson().toJson(this, writer);
            LOGGER.info("Saved config to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }
}
