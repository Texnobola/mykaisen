package com.my.kaisen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.my.kaisen.MyKaisen;
import com.my.kaisen.entity.DismantleProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class DismantleProjectileRenderer extends EntityRenderer<DismantleProjectileEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "textures/particle/dismantle_crescent.png");

    public DismantleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DismantleProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Billboard rotation - face the camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        
        // Flip upright
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Lay flat horizontally as requested
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        
        // Scale based on isGiant variant
        float scale = entity.isGiant() ? 6.0F : 3.0F;
        poseStack.scale(scale, scale, scale);
        
        // Center the matrix BEFORE defining vertices as requested
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        
        // Draw the quad from 0.0F to 1.0F (now centered due to translation)
        vertexconsumer.addVertex(matrix4f, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
        vertexconsumer.addVertex(matrix4f, 1.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
        vertexconsumer.addVertex(matrix4f, 1.0F, 1.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
        vertexconsumer.addVertex(matrix4f, 0.0F, 1.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DismantleProjectileEntity entity) {
        return TEXTURE;
    }
}
