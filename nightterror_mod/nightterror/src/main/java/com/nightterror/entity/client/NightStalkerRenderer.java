package com.nightterror.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightterror.NightTerrorMod;
import com.nightterror.entity.NightStalkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightStalkerRenderer extends GeoEntityRenderer<NightStalkerEntity> {

    public NightStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new NightStalkerModel());
        this.shadowRadius = 0.9f;
    }

    @Override
    public ResourceLocation getTextureLocation(NightStalkerEntity entity) {
        return new ResourceLocation(NightTerrorMod.MOD_ID, "textures/entity/night_stalker.png");
    }

    @Override
    public void render(NightStalkerEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Pulse red tint when enraged
        if (entity.isEnraged()) {
            poseStack.pushPose();
            poseStack.scale(1.15f, 1.15f, 1.15f);
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
