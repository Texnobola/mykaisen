package com.my.kaisen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.my.kaisen.registry.ModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SukunaTattooLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TATTOO_TEXTURE = ResourceLocation.fromNamespaceAndPath("mykaisen", "textures/entity/sukuna_tattoos.png");

    public SukunaTattooLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        // Diagnostic log - if this spans the console, the layer is attached
        System.out.println("TATTOO LAYER RUNNING FOR: " + player.getName().getString());
        
        poseStack.pushPose();
        poseStack.scale(1.01F, 1.01F, 1.01F);
        poseStack.translate(0.0F, -0.015F, 0.0F);
        
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("mykaisen", "textures/entity/sukuna_tattoos.png")));
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        poseStack.popPose();
    }
}
