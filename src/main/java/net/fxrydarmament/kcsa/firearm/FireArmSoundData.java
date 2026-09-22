package net.fxrydarmament.kcsa.firearm;

public class FireArmSoundData {

    private final float volume;
    private final float pitchMin;
    private final float pitchMax;

    public FireArmSoundData(
            float volume,
            float pitchMin,
            float pitchMax
    ) {
        this.volume = volume;
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitchMin() {
        return pitchMin;
    }

    public float getPitchMax() {
        return pitchMax;
    }
}