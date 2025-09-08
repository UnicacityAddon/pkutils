package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.models.config.MainConfig;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.api;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

@Getter
@Setter
public class ConfigService extends PKUtilsBase {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pkutils.json");

    public MainConfig load() {
        if (exists(CONFIG_PATH)) {
            try {
                Reader reader = newBufferedReader(CONFIG_PATH);
                return api.getGson().fromJson(reader, MainConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
            }
        }

        MainConfig mainConfig = new MainConfig();
        save(mainConfig);

        return mainConfig;
    }

    public void save(MainConfig mainConfig) {
        try (Writer writer = newBufferedWriter(CONFIG_PATH)) {
            api.getGson().toJson(mainConfig, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }
}
