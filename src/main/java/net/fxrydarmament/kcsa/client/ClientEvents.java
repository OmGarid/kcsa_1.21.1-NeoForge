package net.fxrydarmament.kcsa.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fxrydarmament.kcsa.ammo.AmmoRenderer;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.fxrydarmament.kcsa.firearm.FireArmRenderer;
import net.fxrydarmament.kcsa.item.ModItems;
import net.fxrydarmament.kcsa.item.custom.HeavyPilotArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "fxrydarmament", value = Dist.CLIENT)
public class ClientEvents {

    // Aim-down-sights FOV zoom
    private static final float AIM_TARGET_FOV = 60f;
    private static final float FOV_LERP_SPEED = 0.2f;

    // Camera recoil scaling applied on top of RecoilHandler's raw values
    private static final float RECOIL_SCALE_AIMING = 1.2f;
    private static final float RECOIL_SCALE_HIPFIRE = 2.4f;

    private static float currentVisualFov = -1f;
    private static float lastAppliedFramePitch = 0f;
    private static float lastAppliedFrameYaw = 0f;

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions heavyPilotArmorExtensions = new IClientItemExtensions() {
            private HeavyPilotArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity livingEntity,
                    ItemStack itemStack,
                    EquipmentSlot equipmentSlot,
                    HumanoidModel<?> original
            ) {
                if (this.renderer == null)
                    this.renderer = new HeavyPilotArmorRenderer();

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        };

        event.registerItem(heavyPilotArmorExtensions, ModItems.HEAVY_PILOT_HELMET.get());
        event.registerItem(heavyPilotArmorExtensions, ModItems.HEAVY_PILOT_CHESTPLATE.get());
        event.registerItem(heavyPilotArmorExtensions, ModItems.HEAVY_PILOT_LEGGINGS.get());
        event.registerItem(heavyPilotArmorExtensions, ModItems.HEAVY_PILOT_BOOTS.get());

        //FireArm
        event.registerItem(new IClientItemExtensions() {
            private FireArmRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new FireArmRenderer();

                return this.renderer;
            }

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack itemStack,
                    float partialTick,
                    float equipProcess,
                    float swingProcess
            ) {
                int i = arm == HumanoidArm.RIGHT ? 1 : -1;

                poseStack.translate(
                        (float) i * 0.7F,
                        -0.52F,
                        -0.85F
                );

                return true;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity livingEntity, InteractionHand hand, ItemStack itemStack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }


        }, ModItems.FIREARM.get());

        // Ammo
        event.registerItem(new IClientItemExtensions() {
            private AmmoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new AmmoRenderer();
                return this.renderer;
            }
        }, ModItems.AMMO.get());
    }



    /*
    *
    *  [ KEY MAPPING AREA ]
    *
    */

    // Fire/Shoot Key
    public static final Lazy<KeyMapping> FIRE_KEY = Lazy.of(() -> new KeyMapping(
            "key.fxrydarmament.fire",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_1,
            "key.categories.fxrydarmament"
    ));

    // Reload Key
    public static final Lazy<KeyMapping> RELOAD_KEY = Lazy.of(() -> new KeyMapping(
            "key.fxrydarmament.reload",
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_R,
            "key.categories.fxrydarmament"
    ));

    // Fire Mode Key
    public static final Lazy<KeyMapping> FIRE_MODE_KEY = Lazy.of(() -> new KeyMapping(
            "key.fxrydarmament.fire_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.fxrydarmament"
    ));

    // Catalog Key
    public static final Lazy<KeyMapping> OPEN_CATALOG_KEY = Lazy.of(() -> new KeyMapping(
            "key.fxrydarmament.open_catalog",
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_F,
            "key.categories.fxrydarmament"
    ));

    // Registers Keys
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(RELOAD_KEY.get());
        event.register(FIRE_KEY.get());
        event.register(FIRE_MODE_KEY.get());
        event.register(OPEN_CATALOG_KEY.get());
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        float baseFov = (float) event.getFOV();

        if (currentVisualFov == -1f) {
            currentVisualFov = baseFov;
        }

        boolean holdingFirearm = player.getMainHandItem().getItem() instanceof FireArmItem;

        // Uses the custom aiming state from FireArmInputHandler
        boolean isAiming = holdingFirearm && FireArmInputHandler.isAiming();

        // Target FOV while aiming down sights.
        // Could be made per-weapon later by reading it from FireArmData instead.
        float targetFOV = isAiming ? AIM_TARGET_FOV : baseFov;

        if (Math.abs(currentVisualFov - targetFOV) > 0.1f) {
            currentVisualFov = Mth.lerp(FOV_LERP_SPEED, currentVisualFov, targetFOV);
        } else {
            currentVisualFov = targetFOV;
        }

        event.setFOV(currentVisualFov);
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 1. Get the partial tick for smooth interpolation at any framerate
        float partialTick = (float) event.getPartialTick();

        // 2. Use RecoilHandler's already-interpolated values for a smooth curve between ticks
        float currentFramePitch = RecoilHandler.getCameraPitch(partialTick);
        float currentFrameYaw = RecoilHandler.getCameraYaw(partialTick);

        // 3. Scale recoil based on aiming state
        boolean aiming = FireArmInputHandler.isAiming();
        float pScale = aiming ? RECOIL_SCALE_AIMING : RECOIL_SCALE_HIPFIRE;
        float yScale = aiming ? RECOIL_SCALE_AIMING : RECOIL_SCALE_HIPFIRE;

        // Target absolute value for this frame
        float targetPitch = currentFramePitch * pScale;
        float targetYaw = currentFrameYaw * yScale;

        // 4. Delta between this frame's target and the previous frame's applied value
        float deltaPitch = targetPitch - lastAppliedFramePitch;
        float deltaYaw = targetYaw - lastAppliedFrameYaw;

        if (Math.abs(deltaPitch) > 0.0001f || Math.abs(deltaYaw) > 0.0001f) {
            // Apply to the player's actual rotation so the crosshair follows the
            // recoil too (true aim punch), not just the camera's visual rotation.
            mc.player.setXRot(mc.player.getXRot() + deltaPitch);
            mc.player.setYRot(mc.player.getYRot() + deltaYaw);
        }

        // 5. Store this frame's value as the baseline for next frame's delta
        lastAppliedFramePitch = targetPitch;
        lastAppliedFrameYaw = targetYaw;
    }

    @SubscribeEvent
    public static void onRenderCrosshair(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean holdingFirearm = player.getMainHandItem().getItem() instanceof FireArmItem;

        if (holdingFirearm && FireArmInputHandler.isAiming()) {
            event.setCanceled(true);
        }
    }




    //ADD NEW EVENTS SOMEWHERE HERE
}
