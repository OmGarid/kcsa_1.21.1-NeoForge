package net.fxrydarmament.kcsa.firearm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class FlatIconRenderer {

    public static void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            ResourceLocation texture
    ) {
        VertexConsumer buffer =
                bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        var pose = poseStack.last().pose();

        float size = 0.5F;

        buffer.addVertex(pose, -size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(packedOverlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);

        buffer.addVertex(pose, size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(packedOverlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);

        buffer.addVertex(pose, size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(packedOverlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);

        buffer.addVertex(pose, -size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(packedOverlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}