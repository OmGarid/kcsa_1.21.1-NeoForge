package net.fxrydarmament.kcsa.firearm;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class FireArmItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public FireArmItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllerRegistrar
    ) {
        AnimationController<FireArmItem> controller =
                new AnimationController<>(
                        this,
                        "idle_controller",
                        0,
                        state -> state.setAndContinue(
                                RawAnimation.begin()
                                        .thenLoop("idle")
                        )
                );

        controller.triggerableAnim("shoot", RawAnimation.begin().thenPlay("shoot"));
        controller.triggerableAnim("reload", RawAnimation.begin().thenPlay("reload"));
        controller.triggerableAnim("reloadempty", RawAnimation.begin().thenPlay("reloadempty"));
        controller.triggerableAnim("draw", RawAnimation.begin().thenPlay("draw"));
        controller.triggerableAnim("holster", RawAnimation.begin().thenPlay("holster"));

        controller.setSoundKeyframeHandler(event -> {
            String sound = event.getKeyframeData().getSound();

            if (sound == null || sound.isBlank()) {
                return;
            }

            var player = Minecraft.getInstance().player;

            if (player == null) {
                return;
            }

            ResourceLocation soundId = ResourceLocation.parse(sound);

            player.playSound(
                    SoundEvent.createVariableRangeEvent(soundId),
                    1.0F,
                    1.0F
            );
        });

        controllerRegistrar.add(controller);
    }

    @Override
    public Component getName(ItemStack stack) {

        ResourceLocation weaponId =
                stack.get(FireArmComponents.FIREARM_ID.get());

        if (weaponId != null) {
            FireArmData data =
                    FireArmDataLoader.get(weaponId);

            if (data != null) {
                return Component.literal(data.getWeaponName());
            }
        }

        return super.getName(stack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());

        if (weaponId != null) {
            FireArmData data = FireArmDataLoader.get(weaponId);

            if (data != null) {
                // Description section
                tooltipComponents.add(Component.literal("Desc:").withStyle(ChatFormatting.GOLD));
                tooltipComponents.add(Component.literal(data.getWeaponDesc()).withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.empty()); // Add a spacer line

                // Ammo section
                var state = stack.get(FireArmComponents.FIREARM_STATE.get());
                int currentAmmo = (state != null) ? state.currentAmmo() : 0;
                tooltipComponents.add(Component.literal("Ammo: " + currentAmmo + "/" + data.getMagazineCapacity())
                        .withStyle(ChatFormatting.WHITE));

                // ID section
                tooltipComponents.add(Component.literal("Firearm ID: " + weaponId.toString())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

}