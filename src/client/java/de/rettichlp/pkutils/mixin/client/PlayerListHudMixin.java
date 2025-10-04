package de.rettichlp.pkutils.mixin.client;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static java.util.Comparator.comparing;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.TextColor.fromFormatting;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.DARK_BLUE;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.RED;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Unique
    private static final Comparator<PlayerListEntry> PKUTILS_ENTRY_ORDERING = comparing((PlayerListEntry playerListEntry) -> {
        String playerName = playerListEntry.getProfile().getName();
        Text displayName = playerListEntry.getDisplayName();

        if (displayName == null) {
            return 6; // OTHER
        }

        List<Text> siblings = displayName.getSiblings();

        if (siblings.isEmpty()) {
            return 6; // OTHER
        }

        TextColor firstSiblingStyleColor = siblings.getFirst().getStyle().getColor();

        if (firstSiblingStyleColor == null) {
            return 6; // OTHER
        }

        if (firstSiblingStyleColor.equals(fromFormatting(DARK_GRAY))) {
            TextColor secondSiblingStyleColor = siblings.get(1).getStyle().getColor();
            if (secondSiblingStyleColor != null && secondSiblingStyleColor.equals(fromFormatting(BLUE))) {
                return 0; // ADMIN
            }

            if (secondSiblingStyleColor != null && secondSiblingStyleColor.equals(fromFormatting(GOLD))) {
                return 5; // REPORT
            }

            return 9;
        } else if (firstSiblingStyleColor.equals(fromFormatting(DARK_BLUE))) {
            return 1; // FBI
        } else if (firstSiblingStyleColor.equals(fromFormatting(BLUE))) {
            return 2; // POLICE
        } else if (firstSiblingStyleColor.equals(fromFormatting(DARK_RED))) {
            return 3; // MEDIC
        } else if (firstSiblingStyleColor.equals(fromFormatting(GOLD))) {
            return 4; // NEWS
        } else if (storage.getWantedEntries().stream().anyMatch(wantedEntry -> wantedEntry.getPlayerName().equals(playerName))) {
            return 6; // WANTED
        } else if (storage.getBlacklistEntries().stream().anyMatch(blacklistEntry -> blacklistEntry.getPlayerName().equals(playerName))) {
            return 7; // BLACKLIST
        } else if (storage.getContractEntries().stream().anyMatch(contractEntry -> contractEntry.getPlayerName().equals(playerName))) {
            return 8; // CONTRACT
        } else {
            return 9; // OTHER
        }
    }).thenComparing(playerListEntry -> playerListEntry.getProfile().getName());

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void onGetPlayerName(PlayerListEntry playerListEntry, @NotNull CallbackInfoReturnable<Text> cir) {
        String playerName = playerListEntry.getProfile().getName();
        Text originText = cir.getReturnValue();

        String icon = "";

        if (storage.getWantedEntries().stream()
                .anyMatch(wantedEntry -> wantedEntry.getPlayerName().equals(playerName))) {
            icon = " 🔍";
        } else if (storage.getBlacklistEntries().stream()
                .anyMatch(blacklistEntry -> blacklistEntry.getPlayerName().equals(playerName))) {
            icon = " 💀";
        }

        if (!icon.isBlank()) {
            cir.setReturnValue(originText.copy().append(" ")
                    .append(of(icon).copy().formatted(RED, BOLD)));
        }
    }

    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    private void onCollectPlayerEntries(@NotNull CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        // get current player list entries
        Collection<PlayerListEntry> playerListEntries = networkHandler.getListedPlayerListEntries();

        // order player list entries
        List<PlayerListEntry> orderedPlayerListEntries = playerListEntries
                .stream()
                .sorted(PKUTILS_ENTRY_ORDERING)
                .limit(80L)
                .toList();

        // set ordered player list entries before the original finally returns them
        cir.setReturnValue(orderedPlayerListEntries);
    }
}
