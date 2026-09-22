package net.fxrydarmament.kcsa.client;

import net.fxrydarmament.kcsa.Config;
import net.fxrydarmament.kcsa.ammo.AmmoItem;
import net.fxrydarmament.kcsa.ammo.component.AmmoComponents;
import net.fxrydarmament.kcsa.firearm.FireArmDataLoader;
import net.fxrydarmament.kcsa.firearm.FireArmSoundData;
import net.fxrydarmament.kcsa.firearm.network.FireModeSwitchPayload;
import net.fxrydarmament.kcsa.firearm.network.FireRequestPayload;
import net.fxrydarmament.kcsa.firearm.FireArmData;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.fxrydarmament.kcsa.firearm.component.FireArmState;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.fxrydarmament.kcsa.firearm.network.ReloadRequestPayload;
import net.fxrydarmament.kcsa.firearm.network.UnloadRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;

// Keybinds
import static net.fxrydarmament.kcsa.client.ClientEvents.RELOAD_KEY;
import static net.fxrydarmament.kcsa.client.ClientEvents.FIRE_KEY;
import static net.fxrydarmament.kcsa.client.ClientEvents.FIRE_MODE_KEY;

@EventBusSubscriber(modid = "fxrydarmament", value = Dist.CLIENT)
public class FireArmInputHandler {

    // Muzzle-particle tuning: Blockbench-unit -> block-space conversion, and
    // per-mode hand offset (relative to the player's eye) used to line the
    // particle spawn point up with the barrel tip. Tweak these to match your model.
    private static final float BLOCKBENCH_UNITS_TO_BLOCKS = 1.0f / 16.0f;
    private static final float HAND_OFFSET_X_HIPFIRE = 0.225f;
    private static final float HAND_OFFSET_Y_HIPFIRE = -0.175f;
    private static final float HAND_OFFSET_Z_HIPFIRE = -1.0f;
    private static final float HAND_OFFSET_X_AIMING = 0.0f;
    private static final float HAND_OFFSET_Y_AIMING = -0.1f;
    private static final float HAND_OFFSET_Z_AIMING = -1.25f;

    // Reload animations are authored at this length (in ticks) in Blockbench;
    // the controller speed is scaled against the weapon's configured reload time
    // so the animation always finishes exactly when the reload does.
    private static final float RELOAD_ANIM_BASE_TICKS = 20f;

    private static boolean wasAttackDown = false;
    private static float fireCooldown = 0f;
    private static int reloadTicks = 0;

    private static boolean isAiming = false;
    private static boolean lastRightClickState = false;

    @SubscribeEvent
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        ItemStack stack = minecraft.player.getMainHandItem();

        if (stack.getItem() instanceof FireArmItem) {
            if (event.isAttack()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        ItemStack heldStack = minecraft.player.getMainHandItem();

        if (!(heldStack.getItem() instanceof FireArmItem fireArmItem)) {
            wasAttackDown = false;
            fireCooldown = 0;
            reloadTicks = 0;
            // Reset recoil when not holding a firearm to stop camera jitter
            RecoilHandler.reset();
            return;
        }

        boolean isAttackDown = FIRE_KEY.get().isDown();

        // Check if right-click is held (using Minecraft's options)
        boolean isRightClickDown = minecraft.options.keyUse.isDown();

        // Handle aiming logic
        handleAiming(isRightClickDown);

        // Handle fire mode switch
        if (FIRE_MODE_KEY.get().consumeClick()) {
            if (heldStack.getItem() instanceof FireArmItem) {
                // Send the payload to the server
                PacketDistributor.sendToServer(new FireModeSwitchPayload());
            }
        }

        // Firing ticks decrement
        if (fireCooldown > 0f) {
            fireCooldown -= 1.0f; // Decrease by one full tick every cycle
        }

        ResourceLocation weaponId =
                heldStack.get(FireArmComponents.FIREARM_ID.get());

        FireArmData data = weaponId != null
                ? FireArmDataLoader.get(weaponId)
                : null;

        FireArmState state =
                heldStack.get(FireArmComponents.FIREARM_STATE.get());

        if (data == null || state == null) {
            wasAttackDown = isAttackDown;
            return;
        }

        // CRITICAL: We must update the recoil physics every single tick
        RecoilHandler.update(data.getRecoilRecovery());

        boolean isAutoMode = "auto".equals(state.currentFireMode());
        boolean shouldFire;

        // Full auto detection
        if (isAutoMode) {
            shouldFire = isAttackDown;
        } else {
            shouldFire = isAttackDown && !wasAttackDown;
        }

        if (reloadTicks > 0) {
            reloadTicks--;
            if (reloadTicks == 0) {
                var manager = fireArmItem.getAnimatableInstanceCache()
                        .getManagerForId(GeoItem.getId(heldStack));
                if (manager != null) {
                    var controller = manager.getAnimationControllers().get("idle_controller");
                    if (controller != null) {
                        controller.setAnimationSpeed(1.0f);
                    }
                }
            }
        }

        // Reload
        if (RELOAD_KEY.get().consumeClick()) {

            if (Screen.hasControlDown()) {
                // CTRL+R -> unload
                PacketDistributor.sendToServer(new UnloadRequestPayload());

            } else if (reloadTicks <= 0) {

                if (state.currentAmmo() < data.getMagazineCapacity()) {

                    ResourceLocation ammoType = data.getAmmoType();

                    // Creative mode, or a weapon with no linked ammo type,
                    // can always reload without checking the inventory.
                    boolean canReload =
                            minecraft.player.isCreative()
                                    || ammoType == null
                                    || hasAmmoInInventory(minecraft.player, ammoType);

                    if (canReload) {
                        triggerReload(fireArmItem, heldStack, data);
                    }
                }
            }
        }

        // Firing
        if (shouldFire && fireCooldown <= 0f && reloadTicks <= 0) {

            triggerFire(fireArmItem, heldStack, data);

            float ticksPerShot =
                    1200f / data.getFireRate();

            fireCooldown += ticksPerShot;
        }

        wasAttackDown = isAttackDown;

        // --- ANIMATION SPEED FAILSAFE ---
        // We only force-reset the speed when we are explicitly reloading.
        // This prevents the 'shoot' animation from slowing down mid-way when the player stops clicking.
        var manager = fireArmItem.getAnimatableInstanceCache().getManagerForId(GeoItem.getId(heldStack));
        if (manager != null && reloadTicks > 0) {
            var controller = manager.getAnimationControllers().get("idle_controller");
            if (controller != null) {
                controller.setAnimationSpeed(1.0f);
            }
        }

        // NEW onClientTick EVENTS ABOVE HERE
    }

    private static void playShootAnimation(
            FireArmItem item,
            ItemStack stack,
            FireArmData data
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        long instanceId = GeoItem.getId(stack);

        float animationLengthTicks = 20.0F;
        float ticksPerShot = 1200.0F / data.getFireRate();

        float speed = animationLengthTicks / ticksPerShot;

        var manager = item.getAnimatableInstanceCache()
                .getManagerForId(instanceId);

        if (manager != null) {
            var controller = manager.getAnimationControllers()
                    .get("idle_controller");

            if (controller != null) {
                controller.stop();
                controller.setAnimationSpeed(speed);
            }
        }

        item.triggerAnim(
                mc.player,
                instanceId,
                "idle_controller",
                "shoot"
        );
    }

    // Trigger firing
    private static void triggerFire(FireArmItem item, ItemStack stack, FireArmData data) {
        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());

        if (state == null || state.currentAmmo() <= 0) {
            return;
        }

        RecoilHandler.addRecoil(data);
        playShootAnimation(item, stack, data);

        // Spawn the muzzle flash particles
        spawnMuzzleParticles();

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        if (weaponId != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                playFirearmSound(mc, weaponId, data, "fire");
                playFirearmSound(mc, weaponId, data, "gunshot_echo");
            }
        }

        PacketDistributor.sendToServer(new FireRequestPayload());
    }

    private static void playFirearmSound(Minecraft mc, ResourceLocation weaponId, FireArmData data, String soundKey) {
        FireArmSoundData soundData = data.getSound(soundKey);
        if (soundData == null) return;

        ResourceLocation soundLoc = ResourceLocation.fromNamespaceAndPath(
                weaponId.getNamespace(),
                weaponId.getPath() + "." + soundKey
        );

        float pitch = randomPitch(soundData.getPitchMin(), soundData.getPitchMax());

        mc.player.playSound(
                SoundEvent.createVariableRangeEvent(soundLoc),
                soundData.getVolume(),
                pitch
        );
    }

    private static float randomPitch(float min, float max) {
        if (max <= min) return min; // avoid a negative/zero range if the JSON min/max are reversed or equal
        return min + RandomSource.create().nextFloat() * (max - min);
    }

    // Trigger reloading
    private static void triggerReload(FireArmItem item, ItemStack stack, FireArmData data) {
        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());

        long instanceId = GeoItem.getId(stack);
        var manager = item.getAnimatableInstanceCache().getManagerForId(instanceId);

        // Assumption: the 'reload' and 'reloadempty' animations are authored at exactly
        // RELOAD_ANIM_BASE_TICKS (1 second / 20 ticks) in Blockbench. If your Blockbench
        // animation length instead matches the JSON reload time directly, set the
        // controller speed to 1.0f instead of scaling it below.

        if (state.currentAmmo() == 0) {
            reloadTicks = data.getReloadEmptyTime();

            // Reload-empty animation speed
            if (manager != null) {
                var controller = manager.getAnimationControllers().get("idle_controller");
                if (controller != null) {
                    controller.setAnimationSpeed(RELOAD_ANIM_BASE_TICKS / (float) data.getReloadEmptyTime());
                }
            }

            assert Minecraft.getInstance().player != null;
            item.triggerAnim(
                    Minecraft.getInstance().player,
                    instanceId,
                    "idle_controller",
                    "reloadempty"
            );
        } else {
            reloadTicks = data.getReloadTime();

            // Tactical reload animation speed
            if (manager != null) {
                var controller = manager.getAnimationControllers().get("idle_controller");
                if (controller != null) {
                    controller.setAnimationSpeed(RELOAD_ANIM_BASE_TICKS / (float) data.getReloadTime());
                }
            }

            assert Minecraft.getInstance().player != null;
            item.triggerAnim(
                    Minecraft.getInstance().player,
                    instanceId,
                    "idle_controller",
                    "reload"
            );
        }

        PacketDistributor.sendToServer(new ReloadRequestPayload());
    }

    private static void handleAiming(boolean isRightClickDown) {
        if (Config.aimMode == Config.AimMode.HOLD) {
            isAiming = isRightClickDown;
        } else {
            // Toggle logic: if right click is pressed and we weren't aiming, toggle on.
            // Note: this is a simple version; usually you'd check for the 'click' event.
            if (isRightClickDown && !wasRightClickDown()) {
                isAiming = !isAiming;
            }
        }
    }

    // Tracks the previous right-click state for the toggle-aim mode above
    private static boolean wasRightClickDown() {
        boolean current = Minecraft.getInstance().options.keyUse.isDown();
        boolean previous = lastRightClickState;
        lastRightClickState = current;
        return previous;
    }

    public static boolean isAiming() {
        return isAiming;
    }

    private static boolean hasAmmoInInventory(Player player, ResourceLocation ammoType) {
        if (ammoType == null) {
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof AmmoItem)) {
                continue;
            }

            ResourceLocation stackAmmoId = stack.get(AmmoComponents.AMMO_ID.get());

            if (ammoType.equals(stackAmmoId) && stack.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    private static void spawnMuzzleParticles() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 muzzleLocal = FireArmBoneCache.getPosition("muzzle_flash");
        if (muzzleLocal == null) return;

        // 1. Scale bone coordinates from Blockbench units to world space
        // (1 Blockbench unit = 1/16 block, unless FireArmBoneCache already divides
        // by 16 elsewhere, in which case drop this conversion).
        float boneX = (float) muzzleLocal.x * BLOCKBENCH_UNITS_TO_BLOCKS;
        float boneY = (float) muzzleLocal.y * BLOCKBENCH_UNITS_TO_BLOCKS;
        float boneZ = (float) muzzleLocal.z * BLOCKBENCH_UNITS_TO_BLOCKS;

        // 2. Hand offset relative to the eye position. The hand sits in a different
        // spot when hipfiring vs. aiming, so this is picked per-mode. Tweak the
        // HAND_OFFSET_* constants above to line this up with your model's barrel tip.
        boolean aiming = isAiming();
        float handOffsetX = aiming ? HAND_OFFSET_X_AIMING : HAND_OFFSET_X_HIPFIRE;
        float handOffsetY = aiming ? HAND_OFFSET_Y_AIMING : HAND_OFFSET_Y_HIPFIRE;
        float handOffsetZ = aiming ? HAND_OFFSET_Z_AIMING : HAND_OFFSET_Z_HIPFIRE;

        // 3. Combine the bone coordinate with the hand offset.
        // Note: GeckoLib's bone Z/X axes are sometimes flipped relative to the
        // Minecraft camera. If the particle spawns mirrored (e.g. drifts left
        // when aiming right), try negating boneZ or boneX here.
        Vector3f offset = new Vector3f(
                boneX + handOffsetX,
                boneY + handOffsetY,
                boneZ + handOffsetZ
        );

        // 4. Rotate to match the camera's look direction
        Quaternionf camRot = mc.gameRenderer.getMainCamera().rotation();
        offset.rotate(camRot);

        // 5. Add to the eye position
        Vec3 eyePos = mc.player.getEyePosition();
        double x = eyePos.x + offset.x;
        double y = eyePos.y + offset.y;
        double z = eyePos.z + offset.z;

        Vec3 lookVec = mc.player.getViewVector(1.0F);

        int particleCount = 3 + mc.player.getRandom().nextInt(4);
        for (int i = 0; i < particleCount; i++) {
            double vx = lookVec.x * 0.15 + (mc.player.getRandom().nextDouble() - 0.5) * 0.05;
            double vy = lookVec.y * 0.15 + (mc.player.getRandom().nextDouble() - 0.5) * 0.05;
            double vz = lookVec.z * 0.15 + (mc.player.getRandom().nextDouble() - 0.5) * 0.05;

            mc.level.addParticle(ParticleTypes.SMOKE, x, y, z, vx * 1.8, vy * 1.8, vz * 1.8);
        }
    }
}
