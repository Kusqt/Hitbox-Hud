package net.hitboxhud.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/**
 * ================================================================
 *  ⚠ HIGHEST VERSION-RISK FILE IN THE PROJECT ⚠
 *
 *  Minecraft's rendering internals (RenderLayer / RenderPhase / the
 *  pipeline builder) were substantially reworked starting in 1.21.2
 *  and have kept shifting in subsequent releases. The exact builder
 *  method chain below reflects the "classic" MultiPhaseParameters
 *  pattern that has been standard for most of 1.21.x, but you MUST
 *  verify this against your actual mapped 1.21.10/1.21.11 sources
 *  (e.g. via Linkie, or by decompiling with the Fabric/Yarn mappings
 *  you're building against) before assuming it compiles as-is.
 *
 *  If RenderLayer.of(...) no longer accepts a MultiPhaseParameters
 *  object in your mapped version, look at how vanilla builds
 *  RenderLayer.getDebugFilledBox() / getLines() in
 *  net.minecraft.client.render.RenderLayer for the current pattern
 *  and mirror it here — the geometry-building code in
 *  HitboxRenderer.java does NOT need to change, only this file.
 * ================================================================
 */
public final class HitboxRenderLayers {

    private HitboxRenderLayers() {}

    /** Non-textured, translucent, double-sided, depth-tested filled quads. */
    public static final RenderLayer TRANSLUCENT_QUADS = RenderLayer.of(
            "hitboxhud_translucent_quads",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            256,
            false, // no texture
            true,  // needs sorting (translucency)
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_COLOR_PROGRAM)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .build(false)
    );

    /** Same pipeline, used for the cyan top-face facing cap so it can be toggled independently. */
    public static final RenderLayer TOP_CAP_QUADS = TRANSLUCENT_QUADS;

    static {
        // Touch RenderSystem so this class initializes render state eagerly on the render thread
        // rather than lazily mid-frame the first time a hitbox is drawn.
        RenderSystem.recordRenderCall(() -> {});
    }
}
