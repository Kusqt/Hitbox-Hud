package net.hitboxhud.mixin;

import net.hitboxhud.config.HitboxConfig;
import net.hitboxhud.render.HitboxRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the vanilla F3+B hitbox renderer for PLAYER entities only.
 * Non-player entities (mobs, item frames, etc.) and the client player's own
 * hitbox are left completely untouched and fall through to vanilla behavior.
 *
 * ⚠ METHOD SIGNATURE NOTE ⚠
 * "renderHitbox" below is the commonly-used Yarn name for this method as of
 * recent 1.21.x releases, taking (MatrixStack, VertexConsumer, Entity, float).
 * Confirm the exact name/descriptor for your specific 1.21.10 / 1.21.11
 * mappings (e.g. with Linkie or by inspecting EntityRenderDispatcher in your
 * decompiled sources) — if it differs, update ONLY the `method = "..."`
 * string below; nothing else in this class needs to change.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "renderHitbox",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hitboxhud$onRenderHitbox(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity,
                                           float tickDelta, CallbackInfo ci) {

        if (!(entity instanceof PlayerEntity)) {
            return; // not a player -> let vanilla draw the normal thin white box
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity self = client.player;

        if (self == null || entity == self) {
            return; // don't touch the client's own hitbox
        }

        // ---- Local-space bounding box -------------------------------------------------
        // Vanilla's MatrixStack passed into renderHitbox is already translated to the
        // entity's interpolated render position, so we build the box relative to the
        // entity's own coordinates (matching vanilla convention) rather than world space.
        Box worldBox = entity.getBoundingBox();
        Box localBox = worldBox.offset(-entity.getX(), -entity.getY(), -entity.getZ());

        // ---- Distance-based wireframe color ---------------------------------------------
        double distance = self.getEyePos().distanceTo(entity.getPos());
        int[] wireColor = HitboxConfig.colorForDistance(distance);

        // ---- Draw ------------------------------------------------------------------------
        if (HitboxConfig.ENABLE_WIREFRAME) {
            HitboxRenderer.drawWireframe(matrices, vertexConsumer, localBox, wireColor);
        }

        if (HitboxConfig.ENABLE_FILL || HitboxConfig.ENABLE_TOP_CAP) {
            VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
            int[] fillColor = HitboxConfig.ENABLE_FILL ? HitboxConfig.FILL_COLOR : new int[]{0, 0, 0, 0};
            int[] capColor = HitboxConfig.ENABLE_TOP_CAP ? HitboxConfig.TOP_CAP_COLOR : fillColor;
            HitboxRenderer.drawFilledBody(matrices, immediate, localBox, fillColor, capColor);
        }

        // Suppress vanilla's white wireframe + blue look-vector line entirely.
        ci.cancel();
    }
}
