package net.fxrydarmament.kcsa.ammo;

import net.fxrydarmament.kcsa.ammo.component.AmmoComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AmmoItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AmmoItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // Intentionally empty — ammo is static, it doesn't need an animation controller at all.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public Component getName(ItemStack stack) {

        ResourceLocation ammoId =
                stack.get(AmmoComponents.AMMO_ID.get());

        if (ammoId != null) {
            AmmoData data =
                    AmmoDataLoader.get(ammoId);

            if (data != null) {
                return Component.literal(data.getAmmoName());
            }
        }

        return super.getName(stack);
    }

}