package net.fxrydarmament.kcsa.ammo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fxrydarmament.kcsa.ammo.component.AmmoComponents;
import net.fxrydarmament.kcsa.firearm.client.FlatIconRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AmmoRenderer extends GeoItemRenderer<AmmoItem> {

    private final AmmoModel ammoModel;

    public AmmoRenderer() {
        super(new AmmoModel());
        this.ammoModel = (AmmoModel) this.model;
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ResourceLocation ammoId = stack.get(AmmoComponents.AMMO_ID.get());

        if (ammoId == null) {
            return; // no data -> nothing to render (safe, doesn't crash)
        }

        this.ammoModel.setAmmoId(ammoId);

        // GUI (inventory/hotbar) and GROUND both render as a flat 2D icon instead of the 3D model
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.0F); // center it in the GUI slot
            renderFlatIcon(ammoId, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        if (transformType == ItemDisplayContext.GROUND) {
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(1.0f, 1.0f, 1.0f);
            renderFlatIcon(ammoId, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderFlatIcon(
            ResourceLocation ammoId,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                ammoId.getNamespace(),
                "textures/ammo/icon/" + ammoId.getPath() + ".png"
        );

        FlatIconRenderer.render(poseStack, bufferSource, packedLight, packedOverlay, iconTexture);
    }
}