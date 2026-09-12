package de.rettichlp.ucutils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.nameTagService;
import static de.rettichlp.ucutils.UCUtils.storage;
import static net.minecraft.ChatFormatting.BOLD;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.TextColor.GOLD;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Unique
    private static final Component AFK_TAG = literal("ᴀꜰᴋ").withColor(GOLD).withStyle(BOLD);

    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void ucutils$submitNameDisplayInvoke(S state,
                                                 PoseStack poseStack,
                                                 SubmitNodeCollector submitNodeCollector,
                                                 CameraRenderState camera,
                                                 int offset,
                                                 CallbackInfo ci) {
        if (!storage.isUnicaCity()) {
            return;
        }

        // extract player name
        // empty[style={color=red,clickEvent=class_10610[command=/tell Maagma46 ],hoverEvent=class_10611[entity=net.minecraft.class_2568$class_5248@a9702189],insertion=Maagma46}, siblings=[literal{[}[style={color=dark_gray}, siblings=[literal{UC}[style={color=blue}], literal{]}[style={color=dark_gray}]]], literal{Maagma46}, empty]]
        Component displayName = state.nameTag;
        if (displayName == null) {
            return;
        }

        String playerName = displayName.getStyle().getInsertion();

        // handle medical information (bandages + pills)
        MutableComponent medicInformation = nameTagService.getMedicInformation(playerName);
        boolean medicInformationPresent = !medicInformation.getSiblings().isEmpty();
        if (configuration.getOptions().nameTag().medicalInformation() && medicInformationPresent) {
            poseStack.pushPose();
            poseStack.scale(0.7f, 0.7f, 0.7f);
            submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment.add(0, 0.6, 0), offset, medicInformation, !state.isDiscrete, state.lightCoords, camera);
            poseStack.popPose();
        }

        // handle afk tag
        if (configuration.getOptions().nameTag().afk() && nameTagService.isAfk(playerName)) {
            poseStack.pushPose();
            poseStack.scale(0.7f, 0.7f, 0.7f);
            submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment.add(0, 1.3, 0), offset, AFK_TAG, !state.isDiscrete, state.lightCoords, camera);
            poseStack.popPose();
        }
    }
}
