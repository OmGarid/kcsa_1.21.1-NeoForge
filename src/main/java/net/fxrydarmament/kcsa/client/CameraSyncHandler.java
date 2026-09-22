package net.fxrydarmament.kcsa.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = "fxrydarmament", value = Dist.CLIENT)
public class CameraSyncHandler {

    // Multiplier applied to the 'camera' bone's rotation before adding it to the
    // player's camera. Scale this or flip an axis's sign if the camera bone is
    // oriented differently in your Blockbench model.
    private static final float CAMERA_BONE_STRENGTH = 60.0f;

    // Recoil render-values are raw (unscaled); this brings them in line with the
    // camera-space scale RecoilHandler.getCameraPitch/Yaw() already apply internally.
    private static final float RECOIL_RENDER_SCALE = 0.3f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {

        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        event.setPitch(event.getPitch() + RecoilHandler.getRenderPitch(partialTick) * RECOIL_RENDER_SCALE);
        event.setYaw(event.getYaw() + RecoilHandler.getRenderYaw(partialTick) * RECOIL_RENDER_SCALE);

        // Add the bone rotation to the player's camera rotation
        event.setYaw(event.getYaw() + -CameraBoneSync.getRotY() * CAMERA_BONE_STRENGTH);
        event.setPitch(event.getPitch() + -CameraBoneSync.getRotX() * CAMERA_BONE_STRENGTH);
        event.setRoll(event.getRoll() + -CameraBoneSync.getRotZ() * CAMERA_BONE_STRENGTH);
    }
}