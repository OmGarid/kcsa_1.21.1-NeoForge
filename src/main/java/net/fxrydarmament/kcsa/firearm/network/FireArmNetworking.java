package net.fxrydarmament.kcsa.firearm.network;

import net.fxrydarmament.kcsa.ammo.AmmoItem;
import net.fxrydarmament.kcsa.ammo.component.AmmoComponents;
import net.fxrydarmament.kcsa.firearm.FireArmData;
import net.fxrydarmament.kcsa.firearm.FireArmDataLoader;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.fxrydarmament.kcsa.firearm.component.FireArmState;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import software.bernie.geckolib.animatable.GeoItem;

public class FireArmNetworking {

    private static final String RELOAD_TICKS_TAG = "fxrydarmament_reload_ticks";

    private static final double MAX_FIRE_RANGE = 100.0;

    // Hit-feedback tuning: particle count/spread and sound pitch/volume
    private static final int ENTITY_HIT_PARTICLE_COUNT = 6;
    private static final double ENTITY_HIT_PARTICLE_SPREAD = 0.1;
    private static final float ENTITY_HIT_SOUND_VOLUME = 0.6f;
    private static final float ENTITY_HIT_SOUND_PITCH = 1.4f;
    private static final int BLOCK_HIT_PARTICLE_COUNT = 4;
    private static final double BLOCK_HIT_PARTICLE_SPREAD = 0.05;

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                FireRequestPayload.TYPE,
                FireRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        handleFireRequest((ServerPlayer) context.player()))
        );

        registrar.playToServer(
                ReloadRequestPayload.TYPE,
                ReloadRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        startReload((ServerPlayer) context.player()))
        );

        registrar.playToServer(
                UnloadRequestPayload.TYPE,
                UnloadRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        unloadAmmo((ServerPlayer) context.player()))
        );

        registrar.playToServer(
                FireModeSwitchPayload.TYPE,
                FireModeSwitchPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        switchFireMode((ServerPlayer) context.player()))
        );
    }

    private static void switchFireMode(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof FireArmItem)) {
            return;
        }

        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());

        if (state == null) {
            return;
        }

        // Cycle fire modes: semi -> auto -> semi (add burst here if needed)
        String nextMode = "semi".equals(state.currentFireMode()) ? "auto" : "semi";

        stack.set(
                FireArmComponents.FIREARM_STATE.get(),
                state.withFireMode(nextMode)
        );
    }

    private static void handleFireRequest(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof FireArmItem fireArmItem)) {
            return;
        }

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        if (weaponId == null) return;

        FireArmData data = FireArmDataLoader.get(weaponId);
        if (data == null) return;

        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());
        if (state == null) return;

        if (state.currentAmmo() <= 0) return;
        if (player.getPersistentData().contains(RELOAD_TICKS_TAG)) return;

        stack.set(
                FireArmComponents.FIREARM_STATE.get(),
                state.withAmmo(state.currentAmmo() - 1)
        );

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(MAX_FIRE_RANGE));

        // 1. Check for blocks along the path, and use the result to clamp the shot range
        BlockHitResult blockHit = player.level().clip(
                new ClipContext(
                        start, end,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        Vec3 effectiveEnd = (blockHit.getType() != HitResult.Type.MISS)
                ? blockHit.getLocation()
                : end;

        // 2. Check for entities, but only along the distance before the wall
        EntityHitResult entityHit = findEntityHit(player, start, effectiveEnd);

        if (entityHit != null) {
            var target = entityHit.getEntity();

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 0; // bypass vanilla's hit i-frames specifically for hitscan
            }

            target.hurt(
                    player.damageSources().playerAttack(player),
                    data.getDamage()
            );

            // 3. Feedback to nearby clients: impact particles + sound
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        entityHit.getLocation().x, entityHit.getLocation().y, entityHit.getLocation().z,
                        ENTITY_HIT_PARTICLE_COUNT,
                        ENTITY_HIT_PARTICLE_SPREAD, ENTITY_HIT_PARTICLE_SPREAD, ENTITY_HIT_PARTICLE_SPREAD,
                        0.0
                );
            }

            player.level().playSound(
                    null,
                    entityHit.getEntity().blockPosition(),
                    SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundSource.PLAYERS,
                    ENTITY_HIT_SOUND_VOLUME, ENTITY_HIT_SOUND_PITCH
            );

        } else if (blockHit.getType() != HitResult.Type.MISS) {
            // 4. Hit a wall/block -> spawn an impact particle there instead of doing nothing
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z,
                        BLOCK_HIT_PARTICLE_COUNT,
                        BLOCK_HIT_PARTICLE_SPREAD, BLOCK_HIT_PARTICLE_SPREAD, BLOCK_HIT_PARTICLE_SPREAD,
                        0.0
                );
            }
        }

        fireArmItem.triggerAnim(
                player,
                GeoItem.getOrAssignId(stack, player.serverLevel()),
                "idle_controller",
                "shoot"
        );
    }

    public static void startReload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof FireArmItem fireArmItem)) return;
        if (player.getPersistentData().contains(RELOAD_TICKS_TAG)) return;

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        if (weaponId == null) return;

        FireArmData data = FireArmDataLoader.get(weaponId);
        if (data == null) return;

        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());
        if (state == null) return;

        if (state.currentAmmo() >= data.getMagazineCapacity()) return;

        ResourceLocation ammoType = data.getAmmoType();

        // Check whether the player has ammo BEFORE starting the animation
        // (it isn't consumed yet — that happens once the reload finishes).
        if (ammoType != null && !player.isCreative()) {
            int available = countAmmo(player, ammoType);
            if (available <= 0) {
                return; // No ammo in inventory -> reload fails, animation never triggers
            }
        }

        int reloadDuration = state.currentAmmo() == 0
                ? data.getReloadEmptyTime()
                : data.getReloadTime();

        player.getPersistentData().putInt(RELOAD_TICKS_TAG, reloadDuration);

        fireArmItem.triggerAnim(
                player,
                GeoItem.getOrAssignId(stack, player.serverLevel()),
                "idle_controller",
                state.currentAmmo() == 0 ? "reloadempty" : "reload"
        );
    }

    // Called every tick (from FireArmServerEvents) to count down the reload timer
    public static void tickReload(ServerPlayer player) {
        if (!player.getPersistentData().contains(RELOAD_TICKS_TAG)) return;

        ItemStack stack = player.getMainHandItem();

        // CANCEL RELOAD: if the player switched items, drop the timer — the ammo
        // is safe since it was never consumed in the first place.
        if (!(stack.getItem() instanceof FireArmItem)) {
            player.getPersistentData().remove(RELOAD_TICKS_TAG);
            return;
        }

        int ticks = player.getPersistentData().getInt(RELOAD_TICKS_TAG) - 1;

        if (ticks > 0) {
            player.getPersistentData().putInt(RELOAD_TICKS_TAG, ticks);
            return;
        }

        // Timer finished: time to actually load the ammo
        player.getPersistentData().remove(RELOAD_TICKS_TAG);

        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());
        if (state == null) return;

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        if (weaponId == null) return;

        FireArmData data = FireArmDataLoader.get(weaponId);
        if (data == null) return;

        int needed = data.getMagazineCapacity() - state.currentAmmo();
        if (needed <= 0) return;

        int targetAmmo;
        ResourceLocation ammoType = data.getAmmoType();

        if (ammoType == null || player.isCreative()) {
            targetAmmo = data.getMagazineCapacity();
        } else {
            // Recount ammo in case the player dropped some while the reload animation was playing
            int available = countAmmo(player, ammoType);
            int toConsume = Math.min(available, needed);

            if (toConsume <= 0) return; // Player dropped all their ammo mid-reload; cancel the refill

            consumeAmmo(player, ammoType, toConsume);
            targetAmmo = state.currentAmmo() + toConsume;
        }

        stack.set(
                FireArmComponents.FIREARM_STATE.get(),
                state.withAmmo(targetAmmo)
        );
    }

    private static EntityHitResult findEntityHit(
            ServerPlayer player,
            Vec3 start,
            Vec3 end
    ) {
        return ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                player.getBoundingBox()
                        .expandTowards(player.getLookAngle().scale(MAX_FIRE_RANGE))
                        .inflate(1.0),
                entity -> !entity.isSpectator()
                        && entity.isPickable()
                        && entity != player
        );
    }

    private static int countAmmo(ServerPlayer player, ResourceLocation ammoType) {
        int total = 0;
        for (ItemStack invStack : player.getInventory().items) {
            if (matchesAmmo(invStack, ammoType)) {
                total += invStack.getCount();
            }
        }
        return total;
    }

    private static void consumeAmmo(ServerPlayer player, ResourceLocation ammoType, int amount) {
        int remaining = amount;
        for (ItemStack invStack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!matchesAmmo(invStack, ammoType)) continue;

            int take = Math.min(remaining, invStack.getCount());
            invStack.shrink(take);
            remaining -= take;
        }
    }

    private static boolean matchesAmmo(ItemStack stack, ResourceLocation ammoType) {
        if (!(stack.getItem() instanceof AmmoItem)) return false;
        ResourceLocation stackAmmoId = stack.get(AmmoComponents.AMMO_ID.get());
        return ammoType.equals(stackAmmoId);
    }

    private static void unloadAmmo(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof FireArmItem)) return;
        if (player.getPersistentData().contains(RELOAD_TICKS_TAG)) return; // can't unload mid-reload

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        if (weaponId == null) return;

        FireArmData data = FireArmDataLoader.get(weaponId);
        if (data == null) return;

        FireArmState state = stack.get(FireArmComponents.FIREARM_STATE.get());
        if (state == null) return;

        int currentAmmo = state.currentAmmo();
        if (currentAmmo <= 0) return; // magazine's already empty, nothing to unload

        ResourceLocation ammoType = data.getAmmoType();

        stack.set(
                FireArmComponents.FIREARM_STATE.get(),
                state.withAmmo(0)
        );

        if (ammoType == null) return; // weapon isn't linked to any ammo item -> the ammo simply vanishes

        int amountToReturn = currentAmmo - 1; // 1 round stays chambered and is lost

        if (amountToReturn <= 0) return; // only the chambered round existed, nothing to return

        ItemStack returnedAmmo = new ItemStack(
                ModItems.AMMO.get(),
                amountToReturn
        );
        returnedAmmo.set(AmmoComponents.AMMO_ID.get(), ammoType);

        if (!player.getInventory().add(returnedAmmo)) {
            player.drop(returnedAmmo, false);
        }
    }
}
