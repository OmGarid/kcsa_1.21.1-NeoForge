package net.fxrydarmament.kcsa.ammo;

import net.minecraft.resources.ResourceLocation;

public class AmmoData {

    private final ResourceLocation ammoId;
    private final String ammoName;
    private final boolean is3d;

    public AmmoData(ResourceLocation ammoId, String ammoName, boolean is3d) {
        this.ammoId = ammoId;
        this.ammoName = ammoName;
        this.is3d = is3d;
    }

    public ResourceLocation getAmmoId() { return ammoId; }
    public String getAmmoName() { return ammoName; }
    public boolean isIs3d() { return is3d; }
}