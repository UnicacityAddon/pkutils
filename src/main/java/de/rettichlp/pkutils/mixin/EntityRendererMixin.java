package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.ContractEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.HousebanEntry;
import de.rettichlp.pkutils.common.models.WantedEntry;
import de.rettichlp.pkutils.common.models.config.NameTagOptions;
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

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.factionService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Color.WHITE;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.RED;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends Entity, T extends EntityRenderState> {

    @ModifyVariable(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text renderLabelIfPresent(Text original, EntityRenderState state) {
        if (!storage.isPunicaKitty()) {
            return original;
        }

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
        NameTagOptions nameTagOptions = configuration.getOptions().nameTag();
        Faction targetFaction = storage.getCachedFaction(targetName);

        Text newTargetDisplayNamePrefix = empty();
        Text newTargetDisplayName = targetDisplayName.copy();
        Text newTargetDisplayNameSuffix = nameTagOptions.factionInformation() ? targetFaction.getNameTagSuffix() : empty();
        Formatting newTargetDisplayNameColor;

        // highlight factions
        newTargetDisplayNameColor = nameTagOptions.highlightFactions().getOrDefault(targetFaction, WHITE).getFormatting();

        // blacklist
        Optional<BlacklistEntry> optionalTargetBlacklistEntry = storage.getBlacklistEntries().stream()
                .filter(blacklistEntry -> blacklistEntry.getPlayerName().equals(targetName))
                .findAny();

        if (optionalTargetBlacklistEntry.isPresent() && nameTagOptions.additionalBlacklist()) {
            newTargetDisplayNameColor = RED;
            newTargetDisplayNamePrefix = !optionalTargetBlacklistEntry.get().isOutlaw() ? empty() : empty()
                    .append(of("[").copy().formatted(DARK_GRAY))
                    .append(of("V").copy().formatted(DARK_RED))
                    .append(of("]").copy().formatted(DARK_GRAY));
        }

        // contract
        Optional<ContractEntry> optionalTargetContractEntry = storage.getContractEntries().stream()
                .filter(contractEntry -> contractEntry.getPlayerName().equals(targetName))
                .findAny();

        if (optionalTargetContractEntry.isPresent() && nameTagOptions.additionalContract()) {
            newTargetDisplayNameColor = RED;
        }

        // houseban
        Optional<HousebanEntry> optionalTargetHousebanEntry = storage.getHousebanEntries().stream()
                .filter(housebanEntry -> housebanEntry.getPlayerName().equals(targetName))
                .filter(housebanEntry -> housebanEntry.getUnbanDateTime().isAfter(now()))
                .findAny();

        if (optionalTargetHousebanEntry.isPresent() && nameTagOptions.additionalHouseban()) {
            newTargetDisplayNamePrefix = empty()
                    .append(of("[").copy().formatted(DARK_GRAY))
                    .append(of("HV").copy().formatted(DARK_RED))
                    .append(of("]").copy().formatted(DARK_GRAY));
        }

        // wanted
        Optional<WantedEntry> optionalTargetWantedEntry = storage.getWantedEntries().stream()
                .filter(wantedEntry -> wantedEntry.getPlayerName().equals(targetName))
                .findAny();

        if (optionalTargetWantedEntry.isPresent() && nameTagOptions.additionalWanted()) {
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
