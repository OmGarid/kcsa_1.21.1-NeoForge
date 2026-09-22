package net.fxrydarmament.kcsa.firearm;

import net.fxrydarmament.kcsa.client.render.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HandsLayer<T extends GeoAnimatable>
        extends GeoRenderLayer<T> {

    private final GeoRenderer<T> renderer;

    public HandsLayer(GeoRenderer<T> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        // Jangan render kalau tidak ada player
        if (minecraft.player == null) {
            return;
        }

        // Cuma render pas item beneran DIPEGANG di first-person hand
        // (otomatis exclude: item frame/FIXED, dropped item/GROUND, GUI, third-person)
        ItemDisplayContext context = ((FireArmRenderer) renderer).getCurrentTransformType();
        boolean isHeldFirstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (!isHeldFirstPerson) {
            return;
        }

         // 1. AMBIL TEXTURE SKIN PLAYER
        ResourceLocation playerSkin =
                minecraft.player.getSkin().texture();


        // 2. BUAT RENDER TYPE MENGGUNAKAN SKIN
        RenderType handRenderType =
                renderer.getRenderType(
                        animatable,
                        playerSkin,
                        bufferSource,
                        partialTick
                );

        // Buffer yang akan menerima geometry tangan.
        VertexConsumer handBuffer =
                bufferSource.getBuffer(handRenderType);

        //3. MASUK KE HANDS PASS
        RenderPass.push(RenderPass.Type.HANDS);

        try {

            // 4. RENDER ULANG MODEL GUN
            renderer.reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    handRenderType,
                    handBuffer,
                    partialTick,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    -1
            );

        } finally {

            // 5. KEMBALI KE MAIN PASS
            RenderPass.pop();
        }
    }
}