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
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (top.theillusivec4.curios.api.CuriosApi.getCuriosHelper().findFirstCurio(player, com.my.kaisen.registry.ModItems.SUKUNA_TATTOO.get()).isPresent()) {
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TATTOO_TEXTURE));
            this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
