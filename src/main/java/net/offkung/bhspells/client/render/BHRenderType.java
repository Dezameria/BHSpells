package net.offkung.bhspells.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BHRenderType extends RenderType {
    private BHRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean crumbling, boolean sort, Runnable setup, Runnable teardown) {
        super(name, format, mode, bufferSize, crumbling, sort, setup, teardown);
    }

    public static RenderType energySwirlOutline(ResourceLocation pLocation, float pU, float pV) {
        return create("bh_energy_swirl_outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false))
                        .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(pU, pV))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true)
        );
    }
}
