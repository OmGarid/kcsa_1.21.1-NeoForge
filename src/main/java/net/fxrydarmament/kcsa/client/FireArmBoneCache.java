package net.fxrydarmament.kcsa.client;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class FireArmBoneCache {
    // Maps Bone Name -> World Position
    private static final Map<String, Vec3> BONE_POSITIONS = new HashMap<>();

    public static void updatePosition(String boneName, Vec3 position) {
        BONE_POSITIONS.put(boneName, position);
    }

    public static Vec3 getPosition(String boneName) {
        return BONE_POSITIONS.get(boneName); // intentionally not getOrDefault(..., Vec3.ZERO) — null means "not cached yet"
    }

}