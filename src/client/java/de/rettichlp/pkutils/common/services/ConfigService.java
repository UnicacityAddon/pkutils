package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.models.config.MainConfig;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

@Getter
@Setter
public class ConfigService extends PKUtilsBase {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pkutils.json");

    public void edit(@NotNull Consumer<MainConfig> mainConfigConsumer) {
        MainConfig mainConfig = load();
        mainConfigConsumer.accept(mainConfig);
        save(mainConfig);
    }

    public MainConfig load() {
        File file = CONFIG_PATH.toFile();

        // create a new config if the file does not exist or is empty
        if (!file.exists() || file.length() == 0) {
            LOGGER.info("Config file does not exist or is empty, creating new one at {}", CONFIG_PATH);
            MainConfig mainConfig = new MainConfig();
            save(mainConfig);
            return mainConfig;
        }

        // load existing config
        try {
            Reader reader = newBufferedReader(CONFIG_PATH);
            return api.getGson().fromJson(reader, MainConfig.class);
        } catch (Exception e) {
            LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
        }

        // fallback
        LOGGER.warn("Failed to load config, using default values");
        MainConfig mainConfig = new MainConfig();
        save(mainConfig);

        return mainConfig;
    }

    private void save(MainConfig mainConfig) {
        try (Writer writer = newBufferedWriter(CONFIG_PATH)) {
            api.getGson().toJson(mainConfig, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }
}
