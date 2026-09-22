package net.fxrydarmament.kcsa.client;

public class CameraBoneSync {

    private static float rotX = 0f;
    private static float rotY = 0f;
    private static float rotZ = 0f;
    private static boolean active = false;

    public static void update(float x, float y, float z) {
        rotX = x;
        rotY = y;
        rotZ = z;
        active = true;
    }

    public static void clear() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static float getRotX() { return rotX; }
    public static float getRotY() { return rotY; }
    public static float getRotZ() { return rotZ; }

    public static float getPitch() { return rotX; }
    public static float getYaw() { return rotY; }
    public static float getRoll() { return rotZ; }
}