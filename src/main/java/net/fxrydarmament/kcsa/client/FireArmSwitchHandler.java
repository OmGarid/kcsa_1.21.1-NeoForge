package net.fxrydarmament.kcsa.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fxrydarmament.kcsa.firearm.FireArmData;
import net.fxrydarmament.kcsa.firearm.FireArmDataLoader;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Objects;

@EventBusSubscriber(modid = "fxrydarmament", value = Dist.CLIENT)
public class FireArmSwitchHandler {

    private static ResourceLocation lastWeaponId = null;
    private static boolean initialized = false;

    // The actual firearm stack currently held, updated every tick while the
    // main hand is a firearm. This is what render overrides read from.
    private static ItemStack cachedActualStack = ItemStack.EMPTY;

    // Stack that is force-rendered while a holster animation is playing.
    private static ItemStack holsterStack = ItemStack.EMPTY;
    private static int holsterTicksRemaining = 0;

    // New firearm whose draw animation is DEFERRED until the holster finishes.
    private static ItemStack pendingDrawStack = ItemStack.EMPTY;

    private static boolean droppedThisTick = false;
    public static boolean isHolstering() {
        return holsterTicksRemaining > 0 && !holsterStack.isEmpty();
    }

    public static ItemStack getHolsterStack() {
        return holsterStack;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // =========================================================
        // DETECT DROP (Q)
        // =========================================================

        droppedThisTick = false;

        while (mc.options.keyDrop.consumeClick()) {
            droppedThisTick = true;
        }

        ItemStack current = mc.player.getMainHandItem();

        ResourceLocation currentWeaponId =
                (current.getItem() instanceof FireArmItem)
                        ? current.get(FireArmComponents.FIREARM_ID.get())
                        : null;


        // =========================================================
        // INITIALIZE
        // =========================================================

        if (!initialized) {

            initialized = true;

            lastWeaponId = currentWeaponId;

            if (currentWeaponId != null) {
                cachedActualStack = current.copy();
            }

        }

        // =========================================================
        // WEAPON CHANGED
        // =========================================================

        else if (!Objects.equals(currentWeaponId, lastWeaponId)) {

            // =====================================================
            // SWITCH AWAY FROM FIREARM
            // =====================================================

            if (lastWeaponId != null && !cachedActualStack.isEmpty()) {

                FireArmData prevData =
                        FireArmDataLoader.get(lastWeaponId);

                if (prevData != null) {

                    // =============================================
                    // DROP -> DON'T PLAY HOLSTER
                    // =============================================

                    if (!droppedThisTick) {

                        holsterStack = cachedActualStack;

                        holsterTicksRemaining =
                                prevData.getHolsterTime();

                        triggerAnim(
                                holsterStack,
                                "holster"
                        );
                    }

                    else {
                        // Item was dropped.
                        // Make sure no old holster state remains.

                        holsterStack = ItemStack.EMPTY;
                        holsterTicksRemaining = 0;
                    }
                }
            }


            // =====================================================
            // SWITCH TO NEW FIREARM
            // =====================================================

            if (currentWeaponId != null) {

                if (isHolstering()) {

                    // Holster first, draw once it's done.
                    pendingDrawStack = current.copy();

                }

                else {

                    // No holster -> draw immediately.

                    FireArmData data =
                            FireArmDataLoader.get(currentWeaponId);

                    if (data != null) {

                        triggerAnim(
                                current,
                                "draw"
                        );
                    }
                }
            }

            else {

                // Switched to non-firearm.
                pendingDrawStack = ItemStack.EMPTY;
            }

            lastWeaponId = currentWeaponId;
        }


        // =========================================================
        // CACHE CURRENT FIREARM
        // =========================================================

        if (currentWeaponId != null) {
            cachedActualStack = current.copy();
        }


        // =========================================================
        // HOLSTER TIMER
        // =========================================================

        if (holsterTicksRemaining > 0) {

            holsterTicksRemaining--;

            if (holsterTicksRemaining == 0) {

                holsterStack = ItemStack.EMPTY;

                if (!pendingDrawStack.isEmpty()) {

                    triggerAnim(
                            pendingDrawStack,
                            "draw"
                    );

                    pendingDrawStack =
                            ItemStack.EMPTY;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!isHolstering()) return;

        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean isLeftMainHand = mc.options.mainHand().get() == HumanoidArm.LEFT;
        HumanoidArm arm = isLeftMainHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        ItemDisplayContext context = isLeftMainHand
                ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Manually apply the firearm's hand transform, because the current main-hand
        // item is no longer a firearm during the holster animation, so vanilla never
        // calls this extension for holsterStack on its own.
        IClientItemExtensions.of(holsterStack).applyForgeHandTransform(
                poseStack,
                mc.player,
                arm,
                holsterStack,
                event.getPartialTick(),
                event.getEquipProgress(),
                event.getSwingProgress()
        );

        mc.gameRenderer.itemInHandRenderer.renderItem(
                mc.player,
                holsterStack,
                context,
                isLeftMainHand,
                poseStack,
                event.getMultiBufferSource(),
                event.getPackedLight()
        );

        poseStack.popPose();
    }

    private static void triggerAnim(ItemStack stack, String animName) {

        if (!(stack.getItem() instanceof FireArmItem item)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        long id = GeoItem.getId(stack);

        var manager =
                item.getAnimatableInstanceCache()
                        .getManagerForId(id);

        if (manager != null) {

            var controller =
                    manager.getAnimationControllers()
                            .get("idle_controller");

            if (controller != null) {

                // =================================================
                // RESET SPEED BEFORE EVERY NEW ANIMATION
                // =================================================

                controller.stop();

                controller.setAnimationSpeed(1.0F);
            }
        }

        // =========================================================
        // PLAY ANIMATION
        // =========================================================

        item.triggerAnim(
                mc.player,
                id,
                "idle_controller",
                animName
        );
    }
}