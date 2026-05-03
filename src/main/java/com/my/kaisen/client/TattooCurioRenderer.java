package com.my.kaisen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.my.kaisen.MyKaisen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class TattooCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TATTOO_TEXTURE = ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "textures/entity/sukuna_tattoos.png");

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (slotContext.entity() instanceof net.minecraft.client.player.AbstractClientPlayer player) {
            net.minecraft.client.renderer.entity.EntityRenderer<?> renderer = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
                net.minecraft.client.model.HumanoidModel<net.minecraft.client.player.AbstractClientPlayer> model = playerRenderer.getModel();
                VertexConsumer vertexconsumer = renderTypeBuffer.getBuffer(RenderType.entityCutoutNoCull(TATTOO_TEXTURE));
                
                // This wraps the tattoo texture perfectly around the arm/leg/torso geometry
                model.renderToBuffer(matrixStack, vertexconsumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        }
    }
}
