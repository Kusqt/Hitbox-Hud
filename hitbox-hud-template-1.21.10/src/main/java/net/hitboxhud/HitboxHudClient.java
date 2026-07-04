package net.hitboxhud;

import net.fabricmc.api.ClientModInitializer;

/**
 * No runtime setup is required — all the work happens in the Mixin.
 * This class exists purely as the declared entrypoint in fabric.mod.json.
 */
public class HitboxHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Intentionally empty. Rendering logic lives entirely in
        // net.hitboxhud.mixin.EntityRenderDispatcherMixin.
    }
}
