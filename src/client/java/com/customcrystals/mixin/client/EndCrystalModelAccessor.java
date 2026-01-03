package com.customcrystals.mixin.client;

import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndCrystalModel.class)
public interface EndCrystalModelAccessor {
    @Accessor("base")
    ModelPart customcrystals$getBase();

    @Accessor("outerGlass")
    ModelPart customcrystals$getOuterGlass();

    @Accessor("innerGlass")
    ModelPart customcrystals$getInnerGlass();

    @Accessor("cube")
    ModelPart customcrystals$getCube();
}