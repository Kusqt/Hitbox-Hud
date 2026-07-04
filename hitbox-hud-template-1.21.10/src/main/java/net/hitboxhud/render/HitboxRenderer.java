package net.hitboxhud.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

/**
 * Pure geometry: builds the wireframe edges, the 6 translucent body-fill
 * quads, and the cyan top-face facing cap. All coordinates are expected to
 * already be LOCAL to the entity (i.e. the MatrixStack passed in has already
 * been translated to the entity's render position by the caller, matching
 * how vanilla's renderHitbox receives its MatrixStack).
 */
public final class HitboxRenderer {

    private HitboxRenderer() {}

    // Corner order: bottom face 0-3 (minY), top face 4-7 (maxY)
    // 0:(minX,minY,minZ) 1:(maxX,minY,minZ) 2:(maxX,minY,maxZ) 3:(minX,minY,maxZ)
    // 4:(minX,maxY,minZ) 5:(maxX,maxY,minZ) 6:(maxX,maxY,maxZ) 7:(minX,maxY,maxZ)

    private static float[][] corners(Box box) {
        return new float[][]{
                {(float) box.minX, (float) box.minY, (float) box.minZ},
                {(float) box.maxX, (float) box.minY, (float) box.minZ},
                {(float) box.maxX, (float) box.minY, (float) box.maxZ},
                {(float) box.minX, (float) box.minY, (float) box.maxZ},
                {(float) box.minX, (float) box.maxY, (float) box.minZ},
                {(float) box.maxX, (float) box.maxY, (float) box.minZ},
                {(float) box.maxX, (float) box.maxY, (float) box.maxZ},
                {(float) box.minX, (float) box.maxY, (float) box.maxZ},
        };
    }

    private static final int[][] EDGES = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, // bottom
            {4, 5}, {5, 6}, {6, 7}, {7, 4}, // top
            {0, 4}, {1, 5}, {2, 6}, {3, 7}  // verticals
    };

    // Each face as 4 corner indices, wound consistently (for the fill quads).
    private static final int[][] FACES = {
            {0, 1, 2, 3}, // bottom
            {4, 5, 6, 7}, // top
            {0, 1, 5, 4}, // north-ish
            {1, 2, 6, 5}, // east-ish
            {2, 3, 7, 6}, // south-ish
            {3, 0, 4, 7}, // west-ish
    };

    /**
     * Draws the 12-edge wireframe using the standard vanilla "lines" vertex
     * consumer (POSITION_COLOR_NORMAL format, matching RenderLayer.getLines()).
     *
     * NOTE ON API STABILITY: the fluent vertex(matrix, x, y, z).color(...).normal(...).next()
     * chain shown here is the long-standing pattern for line-format vertex consumers.
     * If your mapped 1.21.10/1.21.11 sources renamed .next() (e.g. to a no-arg terminal
     * call implied by try-with or a builder pattern) adjust ONLY this method — the
     * corner/edge math above does not need to change.
     */
    public static void drawWireframe(MatrixStack matrices, VertexConsumer lineConsumer, Box box, int[] rgba) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float[][] c = corners(box);
        float r = rgba[0] / 255f, g = rgba[1] / 255f, b = rgba[2] / 255f, a = rgba[3] / 255f;

        for (int[] edge : EDGES) {
            float[] p1 = c[edge[0]];
            float[] p2 = c[edge[1]];
            float nx = p2[0] - p1[0];
            float ny = p2[1] - p1[1];
            float nz = p2[2] - p1[2];
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 1.0e-4f) {
                nx /= len;
                ny /= len;
                nz /= len;
            }

            lineConsumer.vertex(matrix, p1[0], p1[1], p1[2])
                    .color(r, g, b, a)
                    .normal(nx, ny, nz)
                    .next();
            lineConsumer.vertex(matrix, p2[0], p2[1], p2[2])
                    .color(r, g, b, a)
                    .normal(nx, ny, nz)
                    .next();
        }
    }

    /**
     * Draws all 6 faces as translucent solid quads using the custom
     * POSITION_COLOR render layer defined in HitboxRenderLayers.
     * The top face (index 1 in FACES) can be given a distinct color so it
     * doubles as the facing-direction indicator, replacing the vanilla
     * look-vector line entirely.
     */
    public static void drawFilledBody(MatrixStack matrices, VertexConsumerProvider provider, Box box,
                                       int[] bodyRgba, int[] topCapRgba) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float[][] c = corners(box);

        VertexConsumer quads = provider.getBuffer(HitboxRenderLayers.TRANSLUCENT_QUADS);

        for (int faceIdx = 0; faceIdx < FACES.length; faceIdx++) {
            int[] face = FACES[faceIdx];
            boolean isTop = (faceIdx == 1);
            int[] rgba = isTop ? topCapRgba : bodyRgba;
            float r = rgba[0] / 255f, g = rgba[1] / 255f, b = rgba[2] / 255f, a = rgba[3] / 255f;

            for (int idx : face) {
                float[] p = c[idx];
                quads.vertex(matrix, p[0], p[1], p[2])
                        .color(r, g, b, a)
                        .next();
            }
        }
    }
}
