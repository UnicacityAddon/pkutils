package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static net.minecraft.client.sound.PositionedSoundInstance.*;

@Getter
@AllArgsConstructor
public enum Sound {

    BOMB_SOUND("bomb"),
    CONTRACT_FULFILLED("contract.fulfilled"),
    CONTRACT_SET("contract.set"),
    REPORT("report"), // TODO use sound
    SERVICE("service"); // TODO use sound

    private final String path;

    @Contract(" -> new")
    public @NotNull Identifier getIdentifier() {
        return Identifier.of("pkutils", this.path);
    }

    @Contract(" -> new")
    public @NotNull SoundEvent getSoundEvent() {
        return SoundEvent.of(getIdentifier());
    }

    public void play() {
        if (configService.load().getOptions().customSounds()) {
            PositionedSoundInstance positionedSoundInstance = master(getSoundEvent(), 1.0F, 1.0F);
            MinecraftClient.getInstance().getSoundManager().play(positionedSoundInstance);
        }
    }

    public void play(float pitch, float volume) {
        if (configService.load().getOptions().customSounds()) {
            PositionedSoundInstance positionedSoundInstance = master(getSoundEvent(), pitch, volume);
            MinecraftClient.getInstance().getSoundManager().play(positionedSoundInstance);
        }
    }
}
