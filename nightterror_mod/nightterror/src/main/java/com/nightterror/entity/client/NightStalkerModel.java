package com.nightterror.entity.client;

import com.nightterror.NightTerrorMod;
import com.nightterror.entity.NightStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightStalkerModel extends GeoModel<NightStalkerEntity> {

    @Override
    public ResourceLocation getModelResource(NightStalkerEntity entity) {
        return new ResourceLocation(NightTerrorMod.MOD_ID, "geo/night_stalker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NightStalkerEntity entity) {
        return new ResourceLocation(NightTerrorMod.MOD_ID, "textures/entity/night_stalker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NightStalkerEntity entity) {
        return new ResourceLocation(NightTerrorMod.MOD_ID, "animations/night_stalker.animation.json");
    }
}
