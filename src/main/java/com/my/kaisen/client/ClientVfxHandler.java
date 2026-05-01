package com.my.kaisen.client;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FX;
import com.my.kaisen.MyKaisen;
import com.my.kaisen.network.SpawnVfxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side handler for Photon VFX spawn packets.
 * All Photon API calls are client-only and must run on the render thread
 * via context.enqueueWork().
 */
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientVfxHandler {

    /**
     * Receives a SpawnVfxPayload from the server and plays the specified Photon
     * effect at the given world coordinates using BlockEffectExecutor.
     *
     * The .fxproj files must live at:
     *   assets/mykaisen/fx/<effectName>.fxproj
     */
    public static void handleSpawnVfx(SpawnVfxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            ResourceLocation fxLoc = ResourceLocation.fromNamespaceAndPath(
                    MyKaisen.MODID, payload.effectName()
            );

            FX fx = FXHelper.getFX(fxLoc);
            if (fx == null) {
                // Effect file not found – silently skip so a missing asset doesn't crash the client
                return;
            }

            BlockPos pos = BlockPos.containing(payload.x(), payload.y(), payload.z());

            BlockEffectExecutor executor = new BlockEffectExecutor(fx, mc.level, pos);
            // setOffset shifts the effect from the block-center to the exact float position
            executor.setOffset(
                    (float) (payload.x() - pos.getX() - 0.5),
                    (float) (payload.y() - pos.getY()),
                    (float) (payload.z() - pos.getZ() - 0.5)
            );
            executor.setAllowMulti(true); // allow overlapping bursts (e.g. rapid Black Flash chains)
            executor.start();
        });
    }
}
