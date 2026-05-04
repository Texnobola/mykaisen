package com.my.kaisen.client;
 
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
 
public class ShrineRenderer extends GeoEntityRenderer<ShrineEntity> {
    public ShrineRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShrineModel());
    }

    @Override
    public void render(com.my.kaisen.entity.ShrineEntity entity, float entityYaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(6.0F, 6.0F, 6.0F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
