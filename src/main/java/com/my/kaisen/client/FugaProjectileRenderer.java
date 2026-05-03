package com.my.kaisen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.my.kaisen.MyKaisen;
import com.my.kaisen.entity.FugaProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class FugaProjectileRenderer extends EntityRenderer<FugaProjectileEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "textures/entity/fuga_arrow.png");

    public FugaProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FugaProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Align with direction
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // Massive scale + pulsation
        float scale = 4.0F + Mth.sin((entity.tickCount + partialTicks) * 0.4F) * 0.2F;
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();

        // Cross-quad rendering for the arrow
        drawQuad(matrix4f, vertexconsumer, packedLight);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        drawQuad(matrix4f, vertexconsumer, packedLight);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void drawQuad(Matrix4f matrix, VertexConsumer consumer, int light) {
        consumer.addVertex(matrix, -1.0F, -0.25F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 1.0F, -0.25F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 1.0F, 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, -1.0F, 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(FugaProjectileEntity entity) {
        return TEXTURE;
    }
}
