package libreh.manhunt.mixin.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerSkipStartRegionMixin {
    @Inject(at = @At(value = "HEAD"), method = "prepareStartRegion", cancellable = true)
    private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (server.isDedicated()) {
            // It doesn't hurt to cancel start region worldgen.
            ci.cancel();
        }
    }
}
