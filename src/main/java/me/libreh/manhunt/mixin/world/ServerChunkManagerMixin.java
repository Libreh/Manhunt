package me.libreh.manhunt.mixin.world;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.libreh.manhunt.utils.Fields.shouldCancelSaving;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Inject(at = @At(value = "HEAD"), method = "save(Z)V", cancellable = true)
    public void save(boolean flush, CallbackInfo ci) {
        if (shouldCancelSaving) {
            ci.cancel();
        }
    }
}
