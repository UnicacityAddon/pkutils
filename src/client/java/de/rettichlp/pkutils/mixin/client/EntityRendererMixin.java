package de.rettichlp.pkutils.mixin.client;

import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.WantedEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.factionService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.util.Objects.nonNull;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.DARK_BLUE;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.WHITE;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends Entity, T extends EntityRenderState> {

    @ModifyVariable(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text renderLabelIfPresent(Text original, EntityRenderState state) {
        if (state instanceof PlayerEntityRenderState playerEntityRenderState && nonNull(playerEntityRenderState.displayName)) {
            Text targetDisplayName = playerEntityRenderState.displayName;
            String targetName = playerEntityRenderState.name;
            return getFormattedTargetDisplayName(targetDisplayName, targetName);
        } else if (state instanceof ItemEntityRenderState itemDisplayEntityRenderState && nonNull(itemDisplayEntityRenderState.displayName) && itemDisplayEntityRenderState.stack.isOf(Items.SKELETON_SKULL)) {
            Text targetDisplayName = itemDisplayEntityRenderState.displayName;
            String targetName = targetDisplayName.getString().substring(1); // ✞RettichLP -> RettichLP

            LOGGER.debug("Original: {} -> {}, already modified: {}", targetDisplayName.getString(), targetName, targetName.contains(" "));
            // only modify names if not containing space with the faction info prefix - avoid duplicated rendering
            return targetName.contains(" ") ? original : getFormattedTargetDisplayName(targetDisplayName, targetName);
        }

        return original;
    }

    @Unique
    private MutableText getFormattedTargetDisplayName(@NotNull Text targetDisplayName, String targetName) {
        Faction targetFaction = storage.getFaction(targetName);

        Text newTargetDisplayNamePrefix = empty();
        Text newTargetDisplayName = targetDisplayName.copy();
        Text newTargetDisplayNameSuffix = targetFaction.getNameTagSuffix();
        Formatting newTargetDisplayNameColor = WHITE;

        // same faction -> blue name
        Faction playerFaction = storage.getFaction(player.getName().getString());
        if (playerFaction == targetFaction && targetFaction != NULL) {
            newTargetDisplayNameColor = BLUE;
        }

        // alliance faction -> dark blue
        if (configService.load().getAllianceFaction() == targetFaction && targetFaction != NULL) {
            newTargetDisplayNameColor = DARK_BLUE;
        }

        Optional<BlacklistEntry> optionalTargetBlacklistEntry = storage.getBlacklistEntries().stream()
                .filter(blacklistEntry -> blacklistEntry.playerName().equals(targetName))
                .findAny();

        if (optionalTargetBlacklistEntry.isPresent()) {
            newTargetDisplayNameColor = RED;
            newTargetDisplayNamePrefix = !optionalTargetBlacklistEntry.get().getReason().isOutlaw() ? empty() : empty()
                    .append(of("[").copy().formatted(DARK_GRAY))
                    .append(of("V").copy().formatted(DARK_RED))
                    .append(of("]").copy().formatted(DARK_GRAY));
        }

        Optional<WantedEntry> optionalTargetWantedEntry = storage.getWantedEntries().stream()
                .filter(wantedEntry -> wantedEntry.getPlayerName().equals(targetName))
                .findAny();

        if (optionalTargetWantedEntry.isPresent()) {
            newTargetDisplayNameColor = factionService.getWantedPointColor(optionalTargetWantedEntry.get().getWantedPointAmount());
        }

        return empty()
                .append(newTargetDisplayNamePrefix)
                .append(" ")
                .append(newTargetDisplayName.copy().formatted(newTargetDisplayNameColor))
                .append(" ")
                .append(newTargetDisplayNameSuffix);
    }
}
