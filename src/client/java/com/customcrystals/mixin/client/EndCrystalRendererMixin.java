package com.customcrystals.mixin.client;

import com.customcrystals.config.CrystalConfig;
import com.customcrystals.config.CrystalConfigManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalRenderer.class)
public abstract class EndCrystalRendererMixin {

    @ModifyArgs(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V")
    )
    private void customcrystals$scaleCrystal(Args args) {
        CrystalConfig config = CrystalConfigManager.get();
        if (config == null) return;

        float factor = config.scale * (float) args.get(0);
        args.set(0, factor);
        args.set(1, factor);
        args.set(2, factor);
    }

    @ModifyArgs(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")
    )
    private void customcrystals$offsetCrystal(Args args) {
        CrystalConfig config = CrystalConfigManager.get();
        if (config == null) return;

        float yOffset = (float) args.get(1) + config.verticalOffset;
        args.set(1, yOffset);
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    )
    private void customcrystals$colorParts(SubmitNodeCollector collector, Model model, Object stateObj, PoseStack poseStack, RenderType renderType, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        if (!(model instanceof EndCrystalModel crystalModel) || !(stateObj instanceof EndCrystalRenderState) || poseStack == null) {
            collector.submitModel(model, stateObj, poseStack, renderType, light, overlay, color, crumbling);
            return;
        }

        CrystalConfig config = CrystalConfigManager.get();
        EndCrystalModelAccessor accessor = (EndCrystalModelAccessor) crystalModel;
        ModelPart base = accessor.customcrystals$getBase();
        ModelPart outer = accessor.customcrystals$getOuterGlass();
        ModelPart inner = accessor.customcrystals$getInnerGlass();
        ModelPart cube = accessor.customcrystals$getCube();

        boolean baseVisible = base.visible;
        boolean outerVisible = outer.visible;
        boolean innerVisible = inner.visible;
        boolean cubeVisible = cube.visible;

        setVisibility(base, outer, inner, cube, false);

        // Base uses vanilla texture color
        if (baseVisible) {
            base.visible = true;
            collector.submitModel(model, stateObj, poseStack, renderType, light, overlay, 0xFFFFFFFF, crumbling);
            base.visible = false;
        }

        if (cubeVisible) {
            cube.visible = true;
            int packed = pickColor(config == null ? null : config.coreTintEnabled, config == null ? null : config.coreColor);
            collector.submitModel(model, stateObj, poseStack, renderType, light, overlay, packed, crumbling);
            cube.visible = false;
        }

        if (innerVisible) {
            inner.visible = true;
            int packed = pickColor(config == null ? null : config.frame1TintEnabled, config == null ? null : config.frame1Color);
            collector.submitModel(model, stateObj, poseStack, renderType, light, overlay, packed, crumbling);
            inner.visible = false;
        }

        if (outerVisible) {
            outer.visible = true;
            int packed = pickColor(config == null ? null : config.frame2TintEnabled, config == null ? null : config.frame2Color);
            collector.submitModel(model, stateObj, poseStack, renderType, light, overlay, packed, crumbling);
            outer.visible = false;
        }

        base.visible = baseVisible;
        outer.visible = outerVisible;
        inner.visible = innerVisible;
        cube.visible = cubeVisible;
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;F)V",
            at = @At("TAIL")
    )
    private void customcrystals$applyConfig(EndCrystal crystal, EndCrystalRenderState state, float tickDelta, CallbackInfo ci) {
        CrystalConfig config = CrystalConfigManager.get();
        if (config == null) {
            return;
        }

        state.ageInTicks *= config.spinMultiplier;

        if (!config.beamEnabled) {
            state.beamOffset = null;
        } else if (state.beamOffset != null && Math.abs(config.verticalOffset) > 0.0001f) {
            state.beamOffset = state.beamOffset.subtract(new Vec3(0.0, config.verticalOffset, 0.0));
        }
    }

    private static void setVisibility(ModelPart base, ModelPart outer, ModelPart inner, ModelPart cube, boolean value) {
        base.visible = value;
        outer.visible = value;
        inner.visible = value;
        cube.visible = value;
    }

    private static int pickColor(Boolean enabled, Integer color) {
        if (enabled != null && enabled) {
            return 0xFF000000 | (color == null ? 0xFFFFFF : color & 0xFFFFFF);
        }
        return 0xFFFFFFFF;
    }
}
