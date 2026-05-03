package com.my.kaisen.client;
 
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
 
public class ShrineRenderer extends GeoEntityRenderer<ShrineEntity> {
    public ShrineRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShrineModel());
    }
}
