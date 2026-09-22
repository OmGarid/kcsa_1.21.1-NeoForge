package net.fxrydarmament.kcsa.firearm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;
import net.fxrydarmament.kcsa.client.CameraBoneSync;
import net.fxrydarmament.kcsa.client.render.RenderPass;
import net.fxrydarmament.kcsa.firearm.client.FlatIconRenderer;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import net.fxrydarmament.kcsa.client.RecoilHandler;


import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.GeoItemRenderer;


public class FireArmRenderer
        extends GeoItemRenderer<FireArmItem> {

    // Arms Model - Wide (Classic/Steve)
    private static final ResourceLocation RIGHT_ARM_MODEL_WIDE =
            ResourceLocation.fromNamespaceAndPath("fxrydarmament", "geo/firearm/right_arm.geo.json");
    private static final ResourceLocation LEFT_ARM_MODEL_WIDE =
            ResourceLocation.fromNamespaceAndPath("fxrydarmament", "geo/firearm/left_arm.geo.json");

    // Arms Model - Slim (Alex)
    private static final ResourceLocation RIGHT_ARM_MODEL_SLIM =
            ResourceLocation.fromNamespaceAndPath("fxrydarmament", "geo/firearm/right_arm_slim.geo.json");
    private static final ResourceLocation LEFT_ARM_MODEL_SLIM =
            ResourceLocation.fromNamespaceAndPath("fxrydarmament", "geo/firearm/left_arm_slim.geo.json");

    private ResourceLocation resolveArmModel(ResourceLocation wideModel, ResourceLocation slimModel) {
        var player = net.minecraft.client.Minecraft.getInstance().player;

        if (player == null) {
            return wideModel; // fallback aman kalau player null
        }

        boolean isSlimArm = player.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM;
        return isSlimArm ? slimModel : wideModel;
    }

    // define Bone Name
    private static final String RIGHT_ARM_BONE = "rightarm";
    private static final String LEFT_ARM_BONE = "leftarm";
    private static final String AIM_POS_BONE = "aim_pos";

    private ItemDisplayContext currentTransformType = ItemDisplayContext.NONE;
    private final FireArmModel firearmModel;
    private float adsProgress = 0f; // 0 = Hipfire, 1 = ADS


    public ItemDisplayContext getCurrentTransformType() {
        return currentTransformType;
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
        this.currentTransformType = transformType;

        // Use 'stack' instead of 'this.animatable' to avoid NullPointerException
        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON) {
            // Cast the item from the stack to FireArmItem to access the cache
            if (stack.getItem() instanceof FireArmItem fireArmItem) {
                var manager = fireArmItem.getAnimatableInstanceCache().getManagerForId(software.bernie.geckolib.animatable.GeoItem.getId(stack));
                if (manager != null) {
                    var controller = manager.getAnimationControllers().get("idle_controller");
                    if (controller != null) {
                        controller.stop();
                    }
                }
            }
        }

        ResourceLocation weaponId = stack.get(FireArmComponents.FIREARM_ID.get());
        this.firearmModel.setWeaponId(weaponId);

        // Update ADS Progress (Smooth transition using Lerp)
        boolean isAiming = net.fxrydarmament.kcsa.client.FireArmInputHandler.isAiming();
        float targetProgress = isAiming ? 1.0f : 0.0f;

        // Use a lerp factor (0.1f to 0.2f is usually a good sweet spot for "snappy but smooth")
        adsProgress += (targetProgress - adsProgress) * 0.08f;



        // Get FireArmData for recovery speed
        net.fxrydarmament.kcsa.firearm.FireArmData data = weaponId != null
                ? net.fxrydarmament.kcsa.firearm.FireArmDataLoader.get(weaponId)
                : null;


        // GUI (inventory/hotbar) render sebagai flat 2D icon, bukan model 3D
        if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.GROUND && weaponId != null) {
            ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                    weaponId.getNamespace(),
                    "textures/firearm/icon/" + weaponId.getPath() + ".png"
            );

//            System.out.println("ICON TEXTURE = " + iconTexture);



            poseStack.pushPose();
            if (transformType == ItemDisplayContext.GROUND) {
                poseStack.translate(0.25F, 1.0F, 0.5F); //Adjust the GUI Position
            } else {
                poseStack.translate(0.5F, 0.5F, 0.0F);
            }
//            var resource = net.minecraft.client.Minecraft.getInstance()
//                    .getResourceManager()
//                    .getResource(iconTexture);
//
//            System.out.println("ICON EXISTS = " + resource.isPresent());

            FlatIconRenderer.render(poseStack, bufferSource, packedLight, packedOverlay, iconTexture);
            poseStack.popPose();
            return; // skip render 3D
        }

//        if (transformType == ItemDisplayContext.GROUND && weaponId != null) {
//            poseStack.scale(0.8f, 0.8f, 0.8f);
//            poseStack.translate(1.0f, 1.0f, 1.0f);
//        }

        // Apply Procedural Recoil before rendering the 3D model
        if (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {

            // --- ADS LOGIC START ---
            if (adsProgress > 0) {
                // 1. Find the aim_pos bone to calculate offset
                var bakedModel = software.bernie.geckolib.cache.GeckoLibCache.getBakedModels()
                        .get(this.firearmModel.getModelResource(this.animatable));

                if (bakedModel != null) {
                    bakedModel.getBone("aim_pos").ifPresent(bone -> {
                        // IMPORTANT: GeckoLib pivots are in model units.
                        // We must divide by 16.0f to convert them to Minecraft block units.
                        float bx = bone.getPivotX() / 16.0f;
                        float by = bone.getPivotY() / 16.0f;
                        float bz = bone.getPivotZ() / 16.0f;

                        // Minecraft shifts the first-person hand to the right.
                        // We must subtract this shift (usually 0.5F) to reach the actual screen center.
                        float handOffsetx = 0.2317f;
                        float handOffsety = -0.0568f; // -0.0568
                        float handOffsetz = 0.0f;

                        // Adjust translation: subtract the bone position AND the hand offset
                        poseStack.translate((-bx - handOffsetx) * adsProgress, (-by - handOffsety) * adsProgress, (-bz - handOffsetz) * adsProgress);
                    });
                }
            }
            // --- ADS LOGIC END ---

            float partialTick = net.minecraft.client.Minecraft.getInstance()
                    .getTimer().getGameTimeDeltaPartialTick(false);

            float pitch = RecoilHandler.getRenderPitch(partialTick);
            float yaw = RecoilHandler.getRenderYaw(partialTick);
            float roll = RecoilHandler.getRenderRoll(partialTick);

            poseStack.translate(0, 0, -0.6F);

            // TUNED: Reduced from 1.2f to 0.4f for a more controlled kick
            float modelScale = (adsProgress < 1.0f) ? 0.4f : 0.2f;

            // Adjust Pitch, Yaw, and Roll:
            float adjustPitch = 0.01f;
            float adjustYaw = 0.04f;
            float adjustRoll = 1.0f;

            // X-axis: Kick UP
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch * adjustPitch));
            // Y-axis: Kick SIDEWAYS
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw * adjustYaw));
            // Z-axis: Twist (Reduced roll)
            float rollIntensity = (adsProgress < 1.0f) ? 0.3f : 0.1f;
            poseStack.mulPose(Axis.ZP.rotationDegrees((roll * rollIntensity) * adjustRoll ));

            poseStack.translate(0, 0, 0.6F);
        }

        // Custom Viewbobbing
        boolean isFirstPersonView = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (isFirstPersonView) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            var minecraft = net.minecraft.client.Minecraft.getInstance();

            if (player != null && minecraft.options.bobView().get()) {
                float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

                float walkDistance = Mth.lerp(partialTick, player.walkDistO, player.walkDist);
                float bobbing = Mth.lerp(partialTick, player.oBob, player.bob);

                undoVanillaTurnBob(poseStack, player, partialTick);
                applyCustomBob(poseStack, walkDistance, bobbing, 0.8f);
            }
        }


        if ( CameraType.FIRST_PERSON.isFirstPerson() && weaponId != null) {
            var bakedModel = software.bernie.geckolib.cache.GeckoLibCache.getBakedModels()
                    .get(this.firearmModel.getModelResource(this.animatable));

            if (bakedModel == null) {
                System.out.println("DEBUG: bakedModel NULL");
            } else {
                bakedModel.getBone("camera").ifPresentOrElse(
                        cameraBone -> {
                            CameraBoneSync.update(
                                    cameraBone.getRotX(),
                                    cameraBone.getRotY(),
                                    cameraBone.getRotZ()
                            );
                        },
                        () -> System.out.println("DEBUG: bone 'camera' NOT FOUND")
                );

                // TAMBAHAN: capture posisi bone muzzle_flash, sama persis pola di atas
                bakedModel.getBone("muzzle_flash").ifPresentOrElse(
                        muzzleBone -> {
                            float mx = muzzleBone.getPivotX() / 16.0f;
                            float my = muzzleBone.getPivotY() / 16.0f;
                            float mz = muzzleBone.getPivotZ() / 16.0f;

                            net.fxrydarmament.kcsa.client.FireArmBoneCache.updatePosition(
                                    "muzzle_flash",
                                    new net.minecraft.world.phys.Vec3(mx, my, mz)
                            );
                        },
                        () -> System.out.println("DEBUG: bone 'muzzle_flash' NOT FOUND")
                );
            }
        } else {
            CameraBoneSync.clear();
        }




        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);

        // TAMBAHAN: sync rotasi bone "camera" ke player camera, cuma pas first-person
        boolean isFirstPerson = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (isFirstPerson && weaponId != null) {
            var bakedModel = software.bernie.geckolib.cache.GeckoLibCache.getBakedModels()
                    .get(this.firearmModel.getModelResource(this.animatable));

            if (bakedModel == null) {
                System.out.println("DEBUG: bakedModel NULL");
            } else {
                bakedModel.getBone("camera").ifPresentOrElse(
                        cameraBone -> {
                            CameraBoneSync.update(
                                    cameraBone.getRotX(),
                                    cameraBone.getRotY(),
                                    cameraBone.getRotZ()
                            );

                            bakedModel.getBone("muzzle_flash").ifPresent(muzzleBone -> {
                                float mx = muzzleBone.getPivotX() / 16.0f;
                                float my = muzzleBone.getPivotY() / 16.0f;
                                float mz = muzzleBone.getPivotZ() / 16.0f;

                                net.fxrydarmament.kcsa.client.FireArmBoneCache.updatePosition(
                                        "muzzle_flash",
                                        new Vec3(mx, my, mz)
                                );
                            });
                        },
                        () -> System.out.println("DEBUG: bone 'camera' NOT FOUND")
                );
            }
        } else {
            CameraBoneSync.clear();
        }
    }


    // Constructor
    public FireArmRenderer() {
        super(new FireArmModel());

        this.firearmModel = (FireArmModel) this.model;

        addRenderLayer(
                new HandsLayer<>(this)
        );
    }

    // Bone Rendering
    @Override
    public void renderCubesOfBone(
            PoseStack poseStack,
            GeoBone bone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {

        boolean isRightArmBone = RIGHT_ARM_BONE.equals(bone.getName());
        boolean isLeftArmBone = LEFT_ARM_BONE.equals(bone.getName());
        boolean isAimPosBone = AIM_POS_BONE.equals(bone.getName());

        // Bone leftarm/rightarm cuma jadi reference point,
        // mesh aslinya TIDAK PERNAH dirender langsung
        if (isRightArmBone || isLeftArmBone) {

            // Cuma render hand model pengganti pas hands pass
            if (RenderPass.isHands()) {

                if (isRightArmBone) {
                    renderRightArm(
                            poseStack,
                            bone,
                            buffer,
                            packedLight,
                            packedOverlay,
                            color
                    );
                } else {
                    renderLeftArm(
                            poseStack,
                            bone,
                            buffer,
                            packedLight,
                            packedOverlay,
                            color
                    );
                }
            }

            return; // skip mesh asli, baik hands pass maupun main pass
        }

        if(isAimPosBone) {
            return;
        }

        // Hand Pass - bone selain leftarm/rightarm di-skip total
        if (RenderPass.isHands()) {
            return;
        }

        super.renderCubesOfBone(
                poseStack,
                bone,
                buffer,
                packedLight,
                packedOverlay,
                color
        );
    }

    private void renderRightArm(
            PoseStack poseStack,
            GeoBone referenceBone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {

        BakedGeoModel armModel =
                GeckoLibCache.getBakedModels()
                        .get(resolveArmModel(RIGHT_ARM_MODEL_WIDE, RIGHT_ARM_MODEL_SLIM));

        if (armModel == null) {
            return;
        }

        GeoBone armBone =
                armModel.getBone("rightarm")
                        .orElse(null);

        if (armBone == null) {
            return;
        }

        poseStack.pushPose();


        applyArmReferenceTransform(
                poseStack,
                referenceBone,
                armBone
        );


        super.renderCubesOfBone(
                poseStack,
                armBone,
                buffer,
                packedLight,
                packedOverlay,
                color
        );

        poseStack.popPose();
    }


    private void renderLeftArm(
            PoseStack poseStack,
            GeoBone referenceBone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {

        BakedGeoModel armModel =
                GeckoLibCache.getBakedModels()
                        .get(resolveArmModel(LEFT_ARM_MODEL_WIDE, LEFT_ARM_MODEL_SLIM));

        if (armModel == null) {
            return;
        }

        GeoBone armBone =
                armModel.getBone("leftarm")
                        .orElse(null);

        if (armBone == null) {
            return;
        }

        poseStack.pushPose();

        applyArmReferenceTransform(
                poseStack,
                referenceBone,
                armBone
        );

        super.renderCubesOfBone(
                poseStack,
                armBone,
                buffer,
                packedLight,
                packedOverlay,
                color
        );

        poseStack.popPose();
    }


    private void applyArmReferenceTransform(
            PoseStack poseStack,
            GeoBone referenceBone,
            GeoBone armBone
    ) {

        if (referenceBone.getCubes().isEmpty()) {
            return;
        }

        if (armBone.getCubes().isEmpty()) {
            return;
        }

        GeoCube referenceCube =
                referenceBone.getCubes().get(0);

        GeoCube armCube =
                armBone.getCubes().get(0);

        GeoVertex referenceVertex =
                referenceCube.quads()[0].vertices()[0];

        GeoVertex armVertex =
                armCube.quads()[0].vertices()[0];

        double dx =
                referenceVertex.position().x()
                        - armVertex.position().x();

        double dy =
                referenceVertex.position().y()
                        - armVertex.position().y();

        double dz =
                referenceVertex.position().z()
                        - armVertex.position().z();

        poseStack.translate(
                dx,
                dy,
                dz
        );
    }

    private void undoVanillaTurnBob(PoseStack poseStack, LocalPlayer player, float partialTick) {
        float xBob = Mth.lerp(partialTick, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partialTick, player.yBobO, player.yBob);
        float deltaX = (player.getViewXRot(partialTick) - xBob) * 0.1F;
        float deltaY = (player.getViewYRot(partialTick) - yBob) * 0.1F;
        poseStack.mulPose(Axis.YP.rotationDegrees(deltaY));
        poseStack.mulPose(Axis.XP.rotationDegrees(deltaX));
    }


    private void applyCustomBob(PoseStack poseStack, float walkDistance, float bobbing, float pitchMultiplier) {
        boolean isAiming = net.fxrydarmament.kcsa.client.FireArmInputHandler.isAiming();
        float aimReduction = isAiming ? 0.25f : 1.0f; // pas aiming, bobbing turun jadi 25%

        float bobbingPitch = bobbing * 1.0F * pitchMultiplier * aimReduction;
        float bobbingYaw = bobbing * 1.0F * pitchMultiplier * aimReduction;
        float bobbingRoll = bobbing * 1.0F * aimReduction;

        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walkDistance * (float) Math.PI) * bobbingRoll));
        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walkDistance * (float) Math.PI - 0.2F)) * bobbingPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(Math.abs(Mth.cos(walkDistance * (float) Math.PI - 0.2F)) * bobbingYaw));
    }
}