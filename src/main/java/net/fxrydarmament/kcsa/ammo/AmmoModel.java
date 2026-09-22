package net.fxrydarmament.kcsa.ammo;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AmmoModel extends GeoModel<AmmoItem> {

    private ResourceLocation ammoId;

    public void setAmmoId(ResourceLocation ammoId) {
        this.ammoId = ammoId;
    }

    private ResourceLocation getAmmoId() {
        return ammoId;
    }

    @Override
    public ResourceLocation getModelResource(AmmoItem animatable) {

        ResourceLocation ammoId = getAmmoId();

        if (ammoId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "geo/ammo/missing.geo.json"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                ammoId.getNamespace(),
                "geo/ammo/" + ammoId.getPath() + ".geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(AmmoItem animatable) {

        ResourceLocation ammoId = getAmmoId();

        if (ammoId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "textures/ammo/missing.png"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                ammoId.getNamespace(),
                "textures/ammo/" + ammoId.getPath() + ".png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(AmmoItem animatable) {

        ResourceLocation ammoId = getAmmoId();

        if (ammoId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "animations/ammo/missing.animation.json"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                ammoId.getNamespace(),
                "animations/ammo/" + ammoId.getPath() + ".animation.json"
        );
    }
}