package com.my.kaisen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.my.kaisen.MyKaisen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SukunaTattooLayer<T extends AbstractClientPlayer, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TATTOO_TEXTURE = ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "textures/entity/sukuna_tattoos.png");

    public SukunaTattooLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Check if the player is awakened
        if (!livingEntity.getPersistentData().getBoolean("is_awakened")) {
            return;
        }

        // Get the translucent render type
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TATTOO_TEXTURE));

        // Render the model overlay
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), 0xFFFFFFFF);
    }
}
