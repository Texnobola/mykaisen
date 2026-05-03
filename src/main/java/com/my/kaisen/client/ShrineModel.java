package com.my.kaisen.client;
 
import com.my.kaisen.MyKaisen;
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
 
public class ShrineModel extends GeoModel<ShrineEntity> {
    @Override
    public ResourceLocation getModelResource(ShrineEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "geo/shrine.geo.json");
    }
 
    @Override
    public ResourceLocation getTextureResource(ShrineEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "textures/entity/shrine.png");
    }
 
    @Override
    public ResourceLocation getAnimationResource(ShrineEntity animatable) {
        return null;
    }
}
