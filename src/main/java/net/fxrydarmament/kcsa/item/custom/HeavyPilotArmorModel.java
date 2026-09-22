package net.fxrydarmament.kcsa.item.custom;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class HeavyPilotArmorModel extends GeoModel {
    @Override
    public ResourceLocation getModelResource(GeoAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(FXRYDArmament.MOD_ID, "geo/armor/heavy_pilot_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(FXRYDArmament.MOD_ID, "textures/armor/heavy_pilot_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeoAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(FXRYDArmament.MOD_ID, "animations/armor/heavy_pilot_armor.animation.json");
    }
}
