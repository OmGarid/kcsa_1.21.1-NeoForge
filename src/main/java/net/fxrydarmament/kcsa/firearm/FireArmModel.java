package net.fxrydarmament.kcsa.firearm;

import net.fxrydarmament.kcsa.client.FireArmInputHandler;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;


public class FireArmModel extends GeoModel<FireArmItem> {

    private ResourceLocation weaponId;

    public void setWeaponId(ResourceLocation weaponId) {
        this.weaponId = weaponId;
    }

    private ResourceLocation getWeaponId() {
        return weaponId;
    }

    @Override
    public ResourceLocation getModelResource(FireArmItem animatable) {

        ResourceLocation weaponId = getWeaponId();

        if (weaponId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "geo/firearm/missing.geo.json"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                weaponId.getNamespace(),
                "geo/firearm/" + weaponId.getPath() + ".geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(FireArmItem animatable) {

        ResourceLocation weaponId = getWeaponId();

        if (weaponId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "textures/firearm/missing.png"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                weaponId.getNamespace(),
                "textures/firearm/" + weaponId.getPath() + ".png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(FireArmItem animatable) {

        ResourceLocation weaponId = getWeaponId();

        if (weaponId == null) {
            return ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "animations/firearm/missing.animation.json"
            );
        }

        return ResourceLocation.fromNamespaceAndPath(
                weaponId.getNamespace(),
                "animations/firearm/" + weaponId.getPath() + ".animation.json"
        );
    }

    @Override
    public void setCustomAnimations(FireArmItem animatable, long instanceId, AnimationState<FireArmItem> animationState) {
        // Biarkan Geckolib ngitung keyframe aslinya dulu
        super.setCustomAnimations(animatable, instanceId, animationState);

        // Cari bone yang mau lu bajak
        GeoBone gunRoot = this.getAnimationProcessor().getBone("gun_root");
        GeoBone camera = this.getAnimationProcessor().getBone("camera");

        // Kalau bone-nya ketemu dan player lagi ADS
        if (gunRoot != null && FireArmInputHandler.isAiming()) {
            // Model Position
            gunRoot.setPosX(2.0f);
            gunRoot.setPosY(-1.0f);
            gunRoot.setPosZ(12.0f);

            // Model Rotation
            float initialRotX = gunRoot.getInitialSnapshot().getRotX();
            float initialRotY = gunRoot.getInitialSnapshot().getRotY();
            float initialRotZ = gunRoot.getInitialSnapshot().getRotZ();

            float animDeltaX = (gunRoot.getRotX() - initialRotX) * 0.06f;
            float animDeltaY = (gunRoot.getRotY() - initialRotY) * 0.06f;
            float animDeltaZ = (gunRoot.getRotZ() - initialRotZ) * 0.06f;

            gunRoot.setRotX(initialRotX + animDeltaX);
            gunRoot.setRotY(initialRotY + animDeltaY);
            gunRoot.setRotZ(initialRotZ + animDeltaZ);

            float initialCamRotX = camera.getInitialSnapshot().getRotX();
            float initialCamRotY = camera.getInitialSnapshot().getRotY();
            float initialCamRotZ = camera.getInitialSnapshot().getRotZ();

            float animCamDeltaX = (camera.getRotX() - initialRotX) * 0.06f;
            float animCamDeltaY = (camera.getRotY() - initialRotY) * 0.06f;
            float animCamDeltaZ = (camera.getRotZ() - initialRotZ) * 0.06f;

            camera.setRotX(initialRotX + animDeltaX);
            camera.setRotY(initialRotY + animDeltaY);
            camera.setRotZ(initialRotZ + animDeltaZ);
        }
    }


}