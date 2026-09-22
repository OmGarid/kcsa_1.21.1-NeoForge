package net.fxrydarmament.kcsa.client;

import net.fxrydarmament.kcsa.firearm.FireArmData;
import net.minecraft.util.Mth;
import java.util.Random;

public class RecoilHandler {
    private static final Random RANDOM = new Random();

    private static float pitch = 0f;
    private static float yaw = 0f;
    private static float roll = 0f;

    // Previous tick's value, used for render interpolation
    private static float prevPitch = 0f;
    private static float prevYaw = 0f;
    private static float prevRoll = 0f;

    private static float velPitch = 0f;
    private static float velYaw = 0f;
    private static float velRoll = 0f;

    public static void addRecoil(FireArmData data) {
        velPitch = 0;
        velYaw = 0;
        velRoll = 0;

        float randomness = data.getRecoilRandomness();
        float pMult = 1.0f + (RANDOM.nextFloat() * 2.0f - 1.0f) * randomness;
        float yMult = 1.0f + (RANDOM.nextFloat() * 2.0f - 1.0f) * randomness;

        velPitch -= data.getRecoilPitch() * pMult;
        velYaw += (RANDOM.nextBoolean() ? 1 : -1) * data.getRecoilYaw() * yMult;
        velRoll += (RANDOM.nextBoolean() ? 1 : -1) * (data.getRecoilYaw() * 0.5f) * yMult;
    }

    public static void update(float recoverySpeed) {
        // Save the value BEFORE recalculating, as the interpolation baseline
        prevPitch = pitch;
        prevYaw = yaw;
        prevRoll = roll;

        float stiffness = recoverySpeed * 0.3f;
        float damping = 0.75f;

        velPitch += (0 - pitch) * stiffness;
        velYaw += (0 - yaw) * stiffness;
        velRoll += (0 - roll) * stiffness;

        velPitch *= damping;
        velYaw *= damping;
        velRoll *= damping;

        pitch += velPitch;
        yaw += velYaw;
        roll += velRoll;

        if (Math.abs(pitch) < 0.001f && Math.abs(velPitch) < 0.001f) pitch = 0;
        if (Math.abs(yaw) < 0.001f && Math.abs(velYaw) < 0.001f) yaw = 0;
        if (Math.abs(roll) < 0.001f && Math.abs(velRoll) < 0.001f) roll = 0;
    }

    public static void reset() {
        pitch = 0f;
        yaw = 0f;
        roll = 0f;
        prevPitch = 0f;
        prevYaw = 0f;
        prevRoll = 0f;
        velPitch = 0f;
        velYaw = 0f;
        velRoll = 0f;
    }

    public static float getCurrentPitch() { return pitch; }
    public static float getCurrentYaw() { return yaw; }
    public static float getCurrentRoll() { return roll; }

    public static float getCameraPitch(float partialTick) {
        return Mth.lerp(partialTick, prevPitch, pitch) * 0.3f;
    }

    public static float getCameraYaw(float partialTick) {
        return Mth.lerp(partialTick, prevYaw, yaw) * 0.3f;
    }

    // Interpolated version for visual rendering specifically (not physics/camera)
    public static float getRenderPitch(float partialTick) {
        return Mth.lerp(partialTick, prevPitch, pitch);
    }

    public static float getRenderYaw(float partialTick) {
        return Mth.lerp(partialTick, prevYaw, yaw);
    }

    public static float getRenderRoll(float partialTick) {
        return Mth.lerp(partialTick, prevRoll, roll);
    }
}