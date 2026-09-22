package net.fxrydarmament.kcsa.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    // In 1.21.1 Mojang mappings this method is named "bobView"; it takes a
    // PoseStack and a float (partialTick).
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void fxryd_disableFirearmBobbing(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            // Check both the main hand and the offhand
            boolean holdingFirearm = player.getMainHandItem().getItem() instanceof FireArmItem ||
                    player.getOffhandItem().getItem() instanceof FireArmItem;

            if (holdingFirearm) {
                // Cancel the original method to suppress the view-bob effect
                ci.cancel();
            }
        }
    }
}