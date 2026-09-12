package de.rettichlp.ucutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.Nameable;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

import static net.minecraft.network.chat.TextColor.BLUE;
import static net.minecraft.network.chat.TextColor.DARK_AQUA;
import static net.minecraft.network.chat.TextColor.DARK_BLUE;
import static net.minecraft.network.chat.TextColor.DARK_PURPLE;
import static net.minecraft.network.chat.TextColor.DARK_RED;
import static net.minecraft.network.chat.TextColor.GOLD;
import static net.minecraft.network.chat.TextColor.GRAY;
import static net.minecraft.network.chat.TextColor.GREEN;
import static net.minecraft.network.chat.TextColor.LIGHT_PURPLE;
import static net.minecraft.network.chat.TextColor.RED;
import static net.minecraft.network.chat.TextColor.WHITE;
import static net.minecraft.network.chat.TextColor.YELLOW;

@Getter
@AllArgsConstructor
public enum Faction {

    NULL("", "", false, WHITE, ""),
    FBI("FBI", "fbi", false, DARK_BLUE, "✯"),
    POLIZEI("Polizei", "police", false, BLUE, "✯"),
    RETTUNGSDIENST("Rettungsdienst", "medic", false, DARK_RED, "✚"),

    LA_COSA_NOSTRA("La Cosa Nostra", "mafia", true, DARK_AQUA, "⚜"),
    WESTSIDE_BALLAS("Westside Ballas", "gang", true, DARK_PURPLE, "☠"),
    CALDERON_KARTELL("Calderón Kartell", "mexican", true, GOLD, "☀"),
    KERZAKOV_FAMILIE("Kerzakov Familie", "kerzakov", true, RED, "✮"),
    YAKUZA("Yakuza", "yakuza", true, GREEN, "☯"),

    MERCENARY("Söldner", "söldner", false, GRAY, "❇"),
    KIRCHE("Kirche", "church", false, LIGHT_PURPLE, "†"),
    NEWS("News", "news", false, YELLOW, "✉");

    private static final Faction[] CORPSE_FACTIONS = Stream.of(values())
            .filter(faction -> faction != NULL)
            .toArray(Faction[]::new);

    private final String displayName;
    private final String apiKey;
    private final boolean isBadFaction;
    private final TextColor color;
    private final String icon;

    public static @NonNull Faction getFactionByCorpse(@NonNull Nameable itemEntity) {
        if (itemEntity.getCustomName() == null) {
            throw new IllegalArgumentException("ItemEntity has no custom name");
        }

        String corpseName = itemEntity.getCustomName().getString();

        for (Faction faction : CORPSE_FACTIONS) {
            if (corpseName.contains(faction.getIcon())) {
                return faction;
            }
        }

        return NULL;
    }
}
