package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static net.minecraft.text.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@Getter
@AllArgsConstructor
public class BlackMarket {

    private final Type type;
    @Nullable
    private final LocalDateTime visitedAt;
    private boolean found; // whether the black market was found or not

    public Text getText() {
        return empty()
                .styled(style -> style
                        .withHoverEvent(new HoverEvent(SHOW_TEXT, of("Klicke, um die Navigation zu starten").copy().formatted(GOLD)))
                        .withClickEvent(new ClickEvent(RUN_COMMAND, this.type.getNavigationCommand())))
                .append(of(this.type.getDisplayName())).append(" ")
                .append(of("(").copy().formatted(DARK_GRAY))
                .append(of("Besucht:").copy().formatted(GRAY)).append(" ")
                .append(getLastVisitedText())
                .append(of(")").copy().formatted(DARK_GRAY))
                .append(this.found ? of(" ←").copy().formatted(GREEN) : empty());
    }

    private Text getLastVisitedText() {
        if (isNull(this.visitedAt)) {
            return of("Nie").copy().formatted(GRAY);
        }

        Duration duration = Duration.between(this.visitedAt, now());
        long minutes = duration.toMinutes();

        if (minutes >= 60) {
            return of("> 1h").copy().formatted(RED);
        }

        if (minutes == 0) {
            return of("Jetzt").copy().formatted(DARK_GREEN);
        }

        return of("vor " + minutes + (minutes == 1 ? " Minute" : " Minuten")).copy().formatted(YELLOW);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {

        ABANDONED_HOUSE_MINE("Verlassenes Haus (Eisenstollen)", 985, 105, 433),
        AIRPORT_UCA("Flughafen (UCA)", -78, 63, 637),
        ALCATRAZ("Alcatraz", 1154, 83, 695),
        BARRACKS("Baracken", 1796, 71, 373),
        BEACH_MEX("Strand (Mex)", -524, 66, -236),
        BRIDGE_HOUSEADDONSHOP("Brücke (Hausaddonshop)", 80, 58, -11),
        CHINATOWN_HARBOR("Hafen (Chinatown)", 1172, 69, -464),
        CHINATOWN_HOUSE_472("Haus 472 (Chinatown)", 1205, 69, -11),
        CHINATOWN_MILL("Mühle (Chinatown)", 1225, 68, 19),
        CINEMA_RUINS("Kino (Ruine)", 743, 69, 315),
        CONTAINER_HALL("Containerhalle", -93, 69, -43),
        FARM("Farm", 461, 75, 593),
        HARBOR("Hafen", -405, 69, 29),
        HUNTING_JACK("Jagdhütte", 388, 72, -275),
        LAS_UNICAS_AIRPORT("Flughafen Las Unicas", 1694, 69, 557),
        LAS_UNICAS_DANCE_FLOOR("Tanzfläche (Las-Unicas)", 1341, 64, 355),
        LAS_UNICAS_STATE_BANK("Staatsbank (Las-Unicas)", 1466, 64, 145),
        LAS_UNICAS_SWIMMING_POOL("Schwimmbad (Las-Unicas)", 1647, 52, 241),
        MARINA("Yachthafen", 285, 63, -635),
        MEX_SUBWAY("Mex U-Bahn", -92, 52, -33),
        PSYCHIATRIC_HOSPITAL("Psychiatrie", 1689, 66, -390),
        RICE_FIELD("Reisfeld", 1282, 64, -526),
        SHISHABAR("Shishabar", -136, 74, -74),
        SH_PARK_CAVE("SH Park (Höhle)", 64, 67, 347),
        SOCCER_FIELD_GANG("Fußballplatz (Gang)", -468, 69, 425),
        SUBWAY_KERZAKOV("U-Bahn (Kerzakov-Gebiet)", 849, 52, 262),
        UNDER_OLD_MALL_BRIDGE("Unter alter Mall Brücke", 81, 58, -118),
        URANIUM_MOUNTAIN("Uran Berg", -437, 167, 800),
        VIP_LOUNGE("VIP Lounge", -115, 70, -129);

        private final String displayName;
        private final int x;
        private final int y;
        private final int z;

        @Contract(pure = true)
        public @NotNull String getNavigationCommand() {
            return "/navi " + this.x + " " + this.y + " " + this.z;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull BlockPos getBlockPos() {
            return new BlockPos(this.x, this.y, this.z);
        }
    }
}
