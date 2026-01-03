package com.customcrystals.mixin.client;

import com.customcrystals.config.CrystalConfig;
import com.customcrystals.config.CrystalConfigManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to customize End Crystal rendering with per-part coloring.
 * Uses submitCustomGeometry to render each ModelPart with its own color
 * via the traditional ModelPart.render(PoseStack, VertexConsumer, light, overlay, color) method.
 */
@Mixin(EndCrystalRenderer.class)
public abstract class EndCrystalRendererMixin extends EntityRenderer<EndCrystal, EndCrystalRenderState> {

    @Shadow @Final private EndCrystalModel model;
    
    @Shadow
    public static float getY(float ageInTicks) {
        throw new AssertionError();
    }
    
    @Unique
    private static final ResourceLocation CRYSTAL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    
    @Unique
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(CRYSTAL_TEXTURE);

    protected EndCrystalRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void customcrystals$submitInject(EndCrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        CrystalConfig config = CrystalConfigManager.get();
        
        // If no custom rendering needed, let vanilla handle it
        if (config == null || (!config.coreTintEnabled && !config.framesTintEnabled)) {
            return;
        }

        // Cancel vanilla rendering and do our own
        ci.cancel();

        // Get model parts via accessor
        EndCrystalModelAccessor accessor = (EndCrystalModelAccessor) model;
        ModelPart base = accessor.customcrystals$getBase();
        ModelPart cube = accessor.customcrystals$getCube();
        ModelPart innerGlass = accessor.customcrystals$getInnerGlass();
        ModelPart outerGlass = accessor.customcrystals$getOuterGlass();

        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        // Calculate colors (ARGB format with full alpha)
        int coreColor = config.coreTintEnabled ? (0xFF000000 | (config.coreColor & 0xFFFFFF)) : 0xFFFFFFFF;
        int framesColor = config.framesTintEnabled ? (0xFF000000 | (config.framesColor & 0xFFFFFF)) : 0xFFFFFFFF;

        // Apply scale (vanilla uses 2.0)
        float scale = 2.0f * config.scale;
        
        // Get animation parameters from render state
        float yOffset = getY(state.ageInTicks);
        
        // IMPORTANT: Call setupAnim to apply rotations to the model parts!
        // This sets the xRot and yRot on outerGlass, innerGlass, and cube
        model.setupAnim(state);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0f, -0.5f, 0.0f);
        
        // Apply the Y bobbing offset for the floating animation
        float bobOffset = yOffset * 0.5f;
        poseStack.translate(0.0f, bobOffset, 0.0f);

        // Render base using submitCustomGeometry with ModelPart.render()
        if (state.showsBottom && base.visible) {
            final int baseLight = light;
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
                PoseStack tempStack = new PoseStack();
                tempStack.last().pose().set(pose.pose());
                tempStack.last().normal().set(pose.normal());
                base.render(tempStack, vertexConsumer, baseLight, overlay, 0xFFFFFFFF);
            });
        }

        // Render outer glass (frames color)
        if (outerGlass.visible) {
            final int frameLight = light;
            final int frameColor = framesColor;
            
            // Save child visibility and temporarily hide them
            boolean innerWasVisible = innerGlass.visible;
            innerGlass.visible = false;
            
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
                PoseStack tempStack = new PoseStack();
                tempStack.last().pose().set(pose.pose());
                tempStack.last().normal().set(pose.normal());
                outerGlass.render(tempStack, vertexConsumer, frameLight, overlay, frameColor);
            });
            
            innerGlass.visible = innerWasVisible;
        }

        // Render inner glass (frames color)
        if (innerGlass.visible) {
            final int frameLight = light;
            final int frameColor = framesColor;
            
            // Save child visibility and temporarily hide cube
            boolean cubeWasVisible = cube.visible;
            cube.visible = false;
            
            // We need to apply outerGlass's transform to get innerGlass in the right position
            poseStack.pushPose();
            outerGlass.translateAndRotate(poseStack);
            
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
                PoseStack tempStack = new PoseStack();
                tempStack.last().pose().set(pose.pose());
                tempStack.last().normal().set(pose.normal());
                innerGlass.render(tempStack, vertexConsumer, frameLight, overlay, frameColor);
            });
            
            poseStack.popPose();
            cube.visible = cubeWasVisible;
        }

        // Render cube/core (core color)
        if (cube.visible) {
            final int cubeLight = light;
            final int cubeColor = coreColor;
            
            // Apply parent transforms: outerGlass -> innerGlass -> cube
            poseStack.pushPose();
            outerGlass.translateAndRotate(poseStack);
            innerGlass.translateAndRotate(poseStack);
            
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
                PoseStack tempStack = new PoseStack();
                tempStack.last().pose().set(pose.pose());
                tempStack.last().normal().set(pose.normal());
                cube.render(tempStack, vertexConsumer, cubeLight, overlay, cubeColor);
            });
            
            poseStack.popPose();
        }

        poseStack.popPose();

        // Render beam if needed
        if (state.beamOffset != null && config.beamEnabled) {
            float beamYOffset = getY(state.ageInTicks);
            Vec3 beamOffset = state.beamOffset;
            poseStack.translate(beamOffset);
            EnderDragonRenderer.submitCrystalBeams(
                    (float) -beamOffset.x(),
                    (float) -beamOffset.y() + beamYOffset,
                    (float) -beamOffset.z(),
                    state.ageInTicks,
                    poseStack,
                    collector,
                    light
            );
        }

        // Call super for any additional rendering (like name tags)
        super.submit(state, poseStack, collector, cameraState);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;F)V",
            at = @At("TAIL")
    )
    private void customcrystals$applyConfig(EndCrystal crystal, EndCrystalRenderState state, float tickDelta, CallbackInfo ci) {
        // Config adjustments are handled in the submit method
    }
}
