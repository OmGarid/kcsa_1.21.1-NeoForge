package net.fxrydarmament.kcsa.firearm;

import net.minecraft.resources.ResourceLocation;
import java.util.Map;

public class FireArmData {

    private final ResourceLocation weaponId;
    private final String weaponName;
    private final String weaponDesc;
    private final int magazineCapacity;
    private final int damage;
    private final int fireRate;
    private final int reloadTime;
    private final int reloadEmptyTime;
    private final float recoilPitch;
    private final float recoilYaw;
    private final float recoilRecovery;
    private final float recoilRandomness;
    private final Map<String, FireArmSoundData> sounds;
    private final int drawTime;
    private final int holsterTime;
    private final ResourceLocation ammoType; // nullable

    // Constructor
    public FireArmData(
            ResourceLocation weaponId,
            String weaponName,
            String weaponDesc,
            int magazineCapacity,
            int damage,
            int fireRate,
            int reloadTime,
            int reloadEmptyTime,
            float recoilPitch,
            float recoilYaw,
            float recoilRecovery,
            float recoilRandomness,
            Map<String, FireArmSoundData> sounds,
            int drawTime,
            int holsterTime,
            ResourceLocation ammoType
    ) {
        this.weaponId = weaponId;
        this.weaponName = weaponName;
        this.weaponDesc = weaponDesc;
        this.magazineCapacity = magazineCapacity;
        this.damage = damage;
        this.fireRate = fireRate;
        this.reloadTime = reloadTime;
        this.reloadEmptyTime = reloadEmptyTime;
        this.recoilPitch = recoilPitch;
        this.recoilYaw = recoilYaw;
        this.recoilRecovery = recoilRecovery;
        this.recoilRandomness = recoilRandomness;
        this.sounds = sounds;
        this.drawTime = drawTime;
        this.holsterTime = holsterTime;
        this.ammoType = ammoType;
    }

    // Getter
    public ResourceLocation getWeaponId() {
        return weaponId;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getWeaponDesc() {
        return weaponDesc;
    }

    public int getMagazineCapacity() {
        return magazineCapacity;
    }

    public int getDamage() {
        return damage;
    }

    public int getFireRate() {
        return fireRate;
    }

    public int getReloadTime() {
        return reloadTime;
    }

    public int getReloadEmptyTime() {
        return reloadEmptyTime;
    }

    public float getRecoilPitch() {
        return recoilPitch;
    }

    public float getRecoilYaw() {
        return recoilYaw;
    }

    public float getRecoilRecovery() {
        return recoilRecovery;
    }

    public float getRecoilRandomness() {
        return recoilRandomness;
    }

    public int getDrawTime() {
        return drawTime;
    }

    public int getHolsterTime() { return holsterTime; }

    public FireArmSoundData getSound(String soundName) {
        return sounds.get(soundName);
    }

    public Map<String, FireArmSoundData> getSounds() {
        return sounds;
    }

    public ResourceLocation getAmmoType() {return ammoType;}

}