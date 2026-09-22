package net.fxrydarmament.kcsa.mixin;

import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void onSwing(InteractionHand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Get the ItemStack first, then check if its Item is a FireArmItem
        if (entity.getItemInHand(hand).getItem() instanceof FireArmItem) {
            // Cancel the method call to prevent the swing animation from triggering
            ci.cancel();
        }
    }
}