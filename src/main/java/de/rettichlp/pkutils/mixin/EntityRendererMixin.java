package de.rettichlp.pkutils.mixin;

import de.rettichlp.pkutils.common.configuration.options.NameTagOptions;
import de.rettichlp.pkutils.common.models.BlacklistEntry;
import de.rettichlp.pkutils.common.models.ContractEntry;
import de.rettichlp.pkutils.common.models.Faction;
import de.rettichlp.pkutils.common.models.HousebanEntry;
import de.rettichlp.pkutils.common.models.WantedEntry;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.factionService;
import static de.rettichlp.pkutils.PKUtils.networkHandler;
import static de.rettichlp.pkutils.PKUtils.storage;
import static de.rettichlp.pkutils.common.models.Color.WHITE;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL;
import static net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH;
import static net.minecraft.client.render.LightmapTextureManager.applyEmission;
import static net.minecraft.scoreboard.AbstractTeam.CollisionRule.NEVER;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.TextColor.fromFormatting;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.RED;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Final
    @Shadow
    protected EntityRenderDispatcher dispatcher;

    @Final
    @Shadow
    private TextRenderer textRenderer;

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

    @Inject(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void renderLabelIfPresent(EntityRenderState state,
                                      Text original,
                                      MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers,
                                      int light,
                                      CallbackInfo ci) {
        if (!storage.isPunicaKitty()) {
            return;
        }

        if (!(state instanceof PlayerEntityRenderState playerEntityRenderState && nonNull(playerEntityRenderState.displayName))) {
            return;
        }

        String targetName = playerEntityRenderState.name;

        // afk
        if (!configuration.getOptions().nameTag().additionalAfk() || !isAfk(targetName)) {
            return;
        }

        Vec3d vec3d = state.nameLabelPos;
        if (vec3d == null) {
            return;
        }

        boolean sneaking = !state.sneaking;
        matrices.push();
        matrices.translate(vec3d.x, vec3d.y + 0.5F, vec3d.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.02F, -0.02F, 0.02F);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        TextRenderer textRenderer = this.textRenderer;

        Text text = of("ᴀꜰᴋ").copy().formatted(GOLD);

        float x = (-textRenderer.getWidth(text)) / 2.0F;

        textRenderer.draw(text, x, -12.5f, -2130706433, true, matrix4f, vertexConsumers, sneaking ? SEE_THROUGH : NORMAL, 0, light);
        if (sneaking) {
            textRenderer.draw(text, x, -12.5f, -1, true, matrix4f, vertexConsumers, NORMAL, 0, applyEmission(light, 2));
        }

        matrices.pop();
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

    @Unique
    private boolean isAfk(String targetName) {
        return networkHandler.getPlayerList().stream()
                .filter(entry -> entry.getProfile().getName().equals(targetName))
                .anyMatch(entry -> {
                    Team team = entry.getScoreboardTeam();
                    if (team == null) {
                        return false;
                    }

                    if (team.getCollisionRule() != NEVER) {
                        return false; // only afk & aduty players have collision rule set to NEVER
                    }

                    Text displayName = entry.getDisplayName();
                    if (displayName == null) {
                        return false;
                    }

                    List<Text> siblings = displayName.getSiblings();
                    if (siblings.isEmpty()) {
                        return false;
                    }

                    TextColor color = siblings.getFirst().getStyle().getColor();
                    if (color == null) {
                        return false;
                    }

                    return !color.equals(fromFormatting(DARK_GRAY)); // filter out aduty players (dark gray)
                });
    }
}
