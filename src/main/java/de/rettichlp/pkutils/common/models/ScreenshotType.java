package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static java.util.Arrays.stream;
import static net.minecraft.client.util.ScreenshotRecorder.takeScreenshot;
import static net.minecraft.util.Util.getFormattedCurrentTime;

@Getter
@AllArgsConstructor
public enum ScreenshotType {

    ARREST("Verhaftung"),
    BLACKLIST("Blacklist"),
    CORRUPTION("Korruption"),
    DRUG("Drogeneinnahme"),
    EMERGENCY_SERVICE("Notruf"),
    EQUIP("Equip"),
    KILLS("Kills"),
    MAJOR_EVENT("Großeinsatz"),
    OTHER("Andere"),
    REINFORCEMENT("Reinforcement"),
    ROLEPLAY("Roleplay"),
    TICKET("Ticket");

    private static final File RUN_DIRECTORY = MinecraftClient.getInstance().runDirectory;

    private final String displayName;

    public void take(Consumer<File> onSuccess) {
        MinecraftClient.getInstance().execute(() -> {
            try (NativeImage nativeImage = takeScreenshot(MinecraftClient.getInstance().getFramebuffer())) {
                File screenshotFile = getScreenshotFile();
                nativeImage.writeTo(screenshotFile);
                onSuccess.accept(screenshotFile);
            } catch (Exception e) {
                LOGGER.warn("Could not save screenshot", e);
            }
        });
    }

    public @NotNull File getScreenshotDirectory() {
        File file = new File(RUN_DIRECTORY, "pkutils/screenshots/" + this.displayName.toLowerCase());
        file.mkdirs();
        return file;
    }

    private @NotNull File getScreenshotFile() {
        File screenshotDirectory = getScreenshotDirectory();
        String formattedCurrentTime = getFormattedCurrentTime();
        int i = 1;

        while (true) {
            File file = new File(screenshotDirectory, this.displayName.toLowerCase() + "_" + formattedCurrentTime + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }

    public static @NotNull Optional<ScreenshotType> fromDisplayName(String displayName) {
        return stream(values())
                .filter(screenshotType -> displayName.equalsIgnoreCase(screenshotType.getDisplayName()))
                .findFirst();
    }
}
